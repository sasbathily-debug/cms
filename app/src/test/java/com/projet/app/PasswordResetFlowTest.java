package com.projet.app;

import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
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

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * « Mot de passe oublié » : code reçu par e-mail, nouveau mot de passe, ancien mot de passe refusé.
 * Le serveur ne révèle jamais si une adresse a un compte, et un code ne peut pas être deviné par essais.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PasswordResetFlowTest {

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

    private HttpResponse<String> send(HttpClient client, String method, String path, String json) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        CookieManager manager = (CookieManager) client.cookieHandler().orElseThrow();
        manager.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("XSRF-TOKEN"))
                .map(HttpCookie::getValue)
                .findFirst()
                .ifPresent(token -> builder.header("X-XSRF-TOKEN", token));
        HttpRequest.BodyPublisher body = json == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json);
        return client.send(builder.method(method, body).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpClient newClient() throws Exception {
        HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        send(client, "GET", "/site/settings", null);
        return client;
    }

    private void createAccount(String email, String password) throws Exception {
        HttpClient client = newClient();
        assertThat(send(client, "POST", "/auth/register",
                "{\"email\":\"" + email + "\",\"firstName\":\"Awa\",\"lastName\":\"Test\",\"phone\":\"2217" + Math.abs(email.hashCode() % 10_000_000) + "\"}")
                .statusCode()).isEqualTo(202);
        String code = lastCode();
        assertThat(send(client, "POST", "/auth/verify-otp",
                "{\"email\":\"" + email + "\",\"otp\":\"" + code + "\"}").statusCode()).isEqualTo(200);
        assertThat(send(client, "POST", "/auth/complete-registration",
                "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").statusCode()).isEqualTo(200);
    }

    private String lastCode() {
        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, atLeastOnce()).send(sent.capture());
        List<EmailMessage> all = sent.getAllValues();
        Matcher code = Pattern.compile("\\b(\\d{6})\\b").matcher(all.get(all.size() - 1).text());
        assertThat(code.find()).as("code à 6 chiffres dans l'e-mail").isTrue();
        return code.group(1);
    }

    private int login(String email, String password) throws Exception {
        return send(newClient(), "POST", "/auth/login",
                "{\"identifier\":\"" + email + "\",\"password\":\"" + password + "\"}").statusCode();
    }

    @Test
    void leMotDePasseSeReinitialiseAvecLeCodeRecuParEmail() throws Exception {
        String email = "reset.ok@example.com";
        createAccount(email, "AncienMotDePasse1");
        HttpClient client = newClient();

        reset(emailSender);
        assertThat(send(client, "POST", "/auth/forgot-password", "{\"email\":\"" + email + "\"}").statusCode())
                .isEqualTo(204);
        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(sent.capture());
        assertThat(sent.getValue().to()).isEqualTo(email);
        assertThat(sent.getValue().subject()).contains("mot de passe");
        String code = lastCode();

        // un mauvais code ne change rien
        assertThat(send(client, "POST", "/auth/reset-password",
                "{\"email\":\"" + email + "\",\"otp\":\"000000\",\"password\":\"NouveauMotDePasse1\"}")
                .statusCode()).isEqualTo(400);
        assertThat(login(email, "AncienMotDePasse1")).isEqualTo(200);

        // mot de passe trop court refusé
        assertThat(send(client, "POST", "/auth/reset-password",
                "{\"email\":\"" + email + "\",\"otp\":\"" + code + "\",\"password\":\"court\"}")
                .statusCode()).isEqualTo(400);

        assertThat(send(client, "POST", "/auth/reset-password",
                "{\"email\":\"" + email + "\",\"otp\":\"" + code + "\",\"password\":\"NouveauMotDePasse1\"}")
                .statusCode()).isEqualTo(204);
        assertThat(login(email, "NouveauMotDePasse1")).isEqualTo(200);
        assertThat(login(email, "AncienMotDePasse1")).isEqualTo(401);

        // le code ne sert qu'une fois
        assertThat(send(client, "POST", "/auth/reset-password",
                "{\"email\":\"" + email + "\",\"otp\":\"" + code + "\",\"password\":\"EncoreUnAutre12\"}")
                .statusCode()).isEqualTo(400);
    }

    @Test
    void uneAdresseInconnueRecoitLaMemeReponseEtAucunEmail() throws Exception {
        HttpClient client = newClient();
        reset(emailSender);
        assertThat(send(client, "POST", "/auth/forgot-password", "{\"email\":\"personne@example.com\"}").statusCode())
                .isEqualTo(204);
        verify(emailSender, never()).send(org.mockito.ArgumentMatchers.any());

        // même délai de réémission que pour un compte existant : rien ne trahit l'existence du compte
        assertThat(send(client, "POST", "/auth/forgot-password", "{\"email\":\"personne@example.com\"}").statusCode())
                .isEqualTo(429);
        assertThat(send(client, "POST", "/auth/reset-password",
                "{\"email\":\"personne@example.com\",\"otp\":\"123456\",\"password\":\"NouveauMotDePasse1\"}")
                .statusCode()).isEqualTo(400);
    }

    @Test
    void lesEssaisSontLimitesPuisLeCodeEstDetruit() throws Exception {
        String email = "reset.essais@example.com";
        createAccount(email, "AncienMotDePasse1");
        HttpClient client = newClient();
        reset(emailSender);
        send(client, "POST", "/auth/forgot-password", "{\"email\":\"" + email + "\"}");
        String code = lastCode();
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 5; i++) {
            assertThat(send(client, "POST", "/auth/reset-password",
                    "{\"email\":\"" + email + "\",\"otp\":\"" + wrong + "\",\"password\":\"NouveauMotDePasse1\"}")
                    .statusCode()).isEqualTo(400);
        }
        // même le bon code ne fonctionne plus : il faut en redemander un
        assertThat(send(client, "POST", "/auth/reset-password",
                "{\"email\":\"" + email + "\",\"otp\":\"" + code + "\",\"password\":\"NouveauMotDePasse1\"}")
                .statusCode()).isEqualTo(400);
        assertThat(login(email, "AncienMotDePasse1")).isEqualTo(200);
    }
}
