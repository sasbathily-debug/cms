package com.projet.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parcours complet sur une vraie base : ce que le site public voit, ce que seul l'admin peut modifier,
 * et que les brouillons restent invisibles au public.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class SiteContentApiTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @LocalServerPort
    int port;

    /** Un navigateur de test : il garde les cookies et a déjà reçu son jeton CSRF. */
    private HttpClient newClient() throws Exception {
        HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        send(client, "GET", "/site/settings", null);
        return client;
    }

    private HttpResponse<String> send(HttpClient client, String method, String path, String json) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        csrfToken(client).ifPresent(token -> builder.header("X-XSRF-TOKEN", token));
        HttpRequest.BodyPublisher body = json == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json);
        return client.send(builder.method(method, body).build(), HttpResponse.BodyHandlers.ofString());
    }

    private java.util.Optional<String> csrfToken(HttpClient client) {
        CookieManager manager = (CookieManager) client.cookieHandler().orElseThrow();
        return manager.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("XSRF-TOKEN"))
                .map(java.net.HttpCookie::getValue)
                .findFirst();
    }

    private HttpClient adminClient() throws Exception {
        HttpClient client = newClient();
        HttpResponse<String> login = send(client, "POST", "/auth/login",
                "{\"identifier\":\"admin@projet.local\",\"password\":\"Admin123!\"}");
        assertThat(login.statusCode()).isEqualTo(200);
        return client;
    }

    private static String week(String saturdayCloses) {
        StringBuilder days = new StringBuilder();
        for (int day = 1; day <= 5; day++) {
            days.append("{\"dayOfWeek\":").append(day).append(",\"opens\":\"09:00\",\"closes\":\"17:30\",\"closed\":false},");
        }
        days.append("{\"dayOfWeek\":6,\"opens\":\"09:00\",\"closes\":\"").append(saturdayCloses).append("\",\"closed\":false},");
        days.append("{\"dayOfWeek\":7,\"opens\":null,\"closes\":null,\"closed\":true}");
        return "{\"days\":[" + days + "]}";
    }

    @Test
    void lesHorairesSontPublicsEtSeulLAdminLesModifie() throws Exception {
        HttpClient anonymous = newClient();

        HttpResponse<String> read = send(anonymous, "GET", "/site/opening-hours", null);
        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(count(read.body(), "\"dayOfWeek\"")).isEqualTo(7);
        assertThat(read.body()).contains("\"opens\":\"09:00\"").contains("\"closes\":\"17:30\"");

        assertThat(send(anonymous, "PUT", "/site/opening-hours", week("12:00")).statusCode()).isIn(401, 403);

        HttpClient admin = adminClient();
        HttpResponse<String> saved = send(admin, "PUT", "/site/opening-hours", week("12:00"));
        assertThat(saved.statusCode()).isEqualTo(200);
        assertThat(send(anonymous, "GET", "/site/opening-hours", null).body()).contains("\"closes\":\"12:00\"");
    }

    @Test
    void desHorairesIncoherentsSontRefuses() throws Exception {
        HttpClient admin = adminClient();

        HttpResponse<String> reversed = send(admin, "PUT", "/site/opening-hours", week("08:00"));
        assertThat(reversed.statusCode()).isEqualTo(400);
        assertThat(reversed.body()).contains("avant la fermeture");

        HttpResponse<String> incomplete = send(admin, "PUT", "/site/opening-hours",
                "{\"days\":[{\"dayOfWeek\":1,\"opens\":\"09:00\",\"closes\":\"17:00\",\"closed\":false}]}");
        assertThat(incomplete.statusCode()).isEqualTo(400);
    }

    @Test
    void unBrouillonNEstVisibleQueDeLAdmin() throws Exception {
        HttpClient anonymous = newClient();
        HttpClient admin = adminClient();
        String draft = "{\"title\":\"Brouillon secret\",\"body\":\"Texte\",\"icon\":null,\"displayOrder\":50,"
                + "\"category\":\"Diabète\",\"readingMinutes\":5,\"photoUrl\":\"/img/photo-1.jpg\",\"published\":false}";

        HttpResponse<String> created = send(admin, "POST", "/site/health-tips", draft);
        assertThat(created.statusCode()).isEqualTo(201);
        String id = firstGroup(created.body(), "\"id\":\"([^\"]+)\"");

        assertThat(send(anonymous, "GET", "/site/health-tips", null).body()).doesNotContain("Brouillon secret");
        assertThat(send(anonymous, "GET", "/site/health-tips/all", null).statusCode()).isIn(401, 403);
        HttpResponse<String> all = send(admin, "GET", "/site/health-tips/all", null);
        assertThat(all.statusCode()).isEqualTo(200);
        assertThat(all.body()).contains("Brouillon secret").contains("\"readingMinutes\":5");

        String published = draft.replace("\"published\":false", "\"published\":true");
        assertThat(send(admin, "PUT", "/site/health-tips/" + id, published).statusCode()).isEqualTo(200);
        HttpResponse<String> publicList = send(anonymous, "GET", "/site/health-tips", null);
        assertThat(publicList.body()).contains("Brouillon secret").contains("\"category\":\"Diabète\"");
    }

    @Test
    void uneQuestionSansPublicationNEstPasAffichee() throws Exception {
        HttpClient anonymous = newClient();
        HttpClient admin = adminClient();
        String hidden = "{\"question\":\"Question cachée ?\",\"answer\":\"Oui\",\"displayOrder\":60,"
                + "\"category\":\"La consultation\",\"published\":false}";

        assertThat(send(admin, "POST", "/site/faq", hidden).statusCode()).isEqualTo(201);

        assertThat(send(anonymous, "GET", "/site/faq", null).body()).doesNotContain("Question cachée");
        assertThat(send(anonymous, "GET", "/site/faq/all", null).statusCode()).isIn(401, 403);
        assertThat(send(admin, "GET", "/site/faq/all", null).body())
                .contains("Question cachée")
                .contains("\"category\":\"La consultation\"");
    }

    @Test
    void lesAnciensEnvoisSansLesNouveauxChampsRestentValides() throws Exception {
        HttpClient admin = adminClient();
        String old = "{\"title\":\"Ancien format\",\"body\":\"Texte\",\"icon\":null,\"displayOrder\":70}";

        HttpResponse<String> created = send(admin, "POST", "/site/health-tips", old);
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(created.body()).contains("\"published\":true");
    }

    @Test
    void lesReglagesPortentLeRepereEtLeNumeroDUrgence() throws Exception {
        HttpClient admin = adminClient();
        String current = send(admin, "GET", "/site/settings", null).body();
        assertThat(current).contains("\"addressStreet\"").contains("\"landmark\"").contains("\"emergencyNumber\"");
    }

    private static long count(String text, String needle) {
        return List.of(text.split(Pattern.quote(needle), -1)).size() - 1;
    }

    private static String firstGroup(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        assertThat(matcher.find()).as("motif %s dans %s", regex, text).isTrue();
        return matcher.group(1);
    }
}
