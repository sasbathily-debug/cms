package com.projet.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Prendre rendez-vous sans compte : avec une adresse e-mail, il faut le code reçu à cette adresse ; sans adresse,
 * le rendez-vous est enregistré « non vérifié » ; un créneau ne se prend qu'une fois ; les robots et les rafales sont freinés.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class GuestBookingFlowTest {

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

    @MockitoBean
    EmailSender emailSender;

    private static int slot = 0;

    @BeforeEach
    void clean() {
        reset(emailSender);
    }

    private HttpResponse<String> send(HttpClient client, String path, String json, String ip) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .header("X-Forwarded-For", ip);
        CookieManager manager = (CookieManager) client.cookieHandler().orElseThrow();
        manager.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("XSRF-TOKEN"))
                .map(HttpCookie::getValue)
                .findFirst()
                .ifPresent(token -> builder.header("X-XSRF-TOKEN", token));
        return client.send(builder.POST(HttpRequest.BodyPublishers.ofString(json)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpClient newClient() throws Exception {
        HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/site/settings")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        return client;
    }

    private String lastCode() {
        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, atLeastOnce()).send(sent.capture());
        List<EmailMessage> all = sent.getAllValues();
        Matcher code = Pattern.compile("\\b(\\d{6})\\b").matcher(all.get(all.size() - 1).text());
        assertThat(code.find()).as("code à 6 chiffres dans l'e-mail").isTrue();
        return code.group(1);
    }

    /** Un créneau neuf à chaque appel, le même jour pour tous les tests. */
    private String body(String phone, String email, String code, String website) {
        LocalDate date = LocalDate.now().plusDays(3 + (slot / 20));
        String time = String.format("%02d:%02d:00", 8 + (slot % 20) / 2, (slot % 2) * 30);
        slot++;
        return "{\"fullName\":\"Awa Test\",\"phone\":\"" + phone + "\","
                + (email == null ? "" : "\"email\":\"" + email + "\",")
                + "\"reason\":\"Première consultation\",\"preferredDate\":\"" + date + "\",\"preferredTime\":\"" + time + "\""
                + (code == null ? "" : ",\"code\":\"" + code + "\"")
                + (website == null ? "" : ",\"website\":\"" + website + "\"") + "}";
    }

    @Test
    void avecUneAdresseLeCodeRecuParEmailEstExigeEtLeRendezVousEstVerifie() throws Exception {
        HttpClient client = newClient();
        String email = "guest.ok@example.com";

        // sans code : refusé
        assertThat(send(client, "/appointments/guest", body("221770000001", email, null, null), "10.0.0.1").statusCode())
                .isEqualTo(400);

        assertThat(send(client, "/appointments/guest/code",
                "{\"email\":\"" + email + "\",\"fullName\":\"Awa Test\"}", "10.0.0.1").statusCode()).isEqualTo(204);
        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(sent.capture());
        assertThat(sent.getValue().to()).isEqualTo(email);
        assertThat(sent.getValue().subject()).contains("rendez-vous");
        String code = lastCode();

        // mauvais code : refusé
        assertThat(send(client, "/appointments/guest", body("221770000001", email, "000000", null), "10.0.0.1").statusCode())
                .isEqualTo(400);

        reset(emailSender);
        HttpResponse<String> ok = send(client, "/appointments/guest", body("221770000001", email, code, null), "10.0.0.1");
        assertThat(ok.statusCode()).isEqualTo(201);
        assertThat(ok.body()).contains("\"contactVerified\":true").contains("\"status\":\"CONFIRMED\"");
        // la confirmation du rendez-vous part par e-mail
        verify(emailSender, atLeastOnce()).send(any(EmailMessage.class));

        // un code ne sert qu'une fois
        assertThat(send(client, "/appointments/guest", body("221770000001", email, code, null), "10.0.0.1").statusCode())
                .isEqualTo(400);
    }

    @Test
    void sansAdresseEmailLeRendezVousEstEnregistreNonVerifie() throws Exception {
        HttpClient client = newClient();
        HttpResponse<String> ok = send(client, "/appointments/guest", body("221770000002", null, null, null), "10.0.0.2");
        assertThat(ok.statusCode()).isEqualTo(201);
        assertThat(ok.body()).contains("\"contactVerified\":false");
    }

    @Test
    void unCreneauNeSePrendQuUneFois() throws Exception {
        HttpClient client = newClient();
        String payload = body("221770000003", null, null, null);
        assertThat(send(client, "/appointments/guest", payload, "10.0.0.3").statusCode()).isEqualTo(201);
        assertThat(send(client, "/appointments/guest", payload.replace("221770000003", "221770000099"), "10.0.0.4")
                .statusCode()).isEqualTo(409);
    }

    @Test
    void leChampPiegeEtLesRafalesSontFreines() throws Exception {
        HttpClient client = newClient();
        // champ piège rempli : refus
        assertThat(send(client, "/appointments/guest", body("221770000004", null, null, "http://spam"), "10.0.0.5")
                .statusCode()).isEqualTo(400);
        // même téléphone : 3 rendez-vous par jour au plus
        int refuses = 0;
        for (int i = 0; i < 5; i++) {
            int status = send(client, "/appointments/guest", body("221770000005", null, null, null), "10.0.0." + (20 + i)).statusCode();
            if (status == 429) refuses++;
        }
        assertThat(refuses).isEqualTo(2);
    }
}
