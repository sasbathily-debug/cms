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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Le parcours de création de compte tel que l'ancien site le fait : inscription (e-mail, téléphone, noms),
 * code reçu par e-mail, vérification, mot de passe, puis connexion. Le serveur ne doit pas avoir changé de comportement.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RegistrationFlowTest {

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

    @Test
    void lInscriptionParCodeEnvoyeParEmailFonctionneDeBoutEnBout() throws Exception {
        HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        send(client, "GET", "/site/settings", null);
        String email = "awa.test@example.com";

        HttpResponse<String> register = send(client, "POST", "/auth/register",
                "{\"email\":\"" + email + "\",\"firstName\":\"Awa\",\"lastName\":\"Test\",\"phone\":\"221771234567\"}");
        assertThat(register.statusCode()).isEqualTo(202);

        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, atLeastOnce()).send(sent.capture());
        EmailMessage otpMail = sent.getValue();
        assertThat(otpMail.to()).isEqualTo(email);
        assertThat(otpMail.replyTo()).isNull();
        Matcher code = Pattern.compile("\\b(\\d{6})\\b").matcher(otpMail.text());
        assertThat(code.find()).as("code à 6 chiffres dans l'e-mail : %s", otpMail.text()).isTrue();

        HttpResponse<String> wrong = send(client, "POST", "/auth/verify-otp",
                "{\"email\":\"" + email + "\",\"otp\":\"000000\"}");
        assertThat(wrong.statusCode()).isBetween(400, 499);

        HttpResponse<String> verified = send(client, "POST", "/auth/verify-otp",
                "{\"email\":\"" + email + "\",\"otp\":\"" + code.group(1) + "\"}");
        assertThat(verified.statusCode()).isEqualTo(200);

        HttpResponse<String> completed = send(client, "POST", "/auth/complete-registration",
                "{\"email\":\"" + email + "\",\"password\":\"MotDePasse123\"}");
        assertThat(completed.statusCode()).isEqualTo(200);
        assertThat(completed.body()).contains(email);

        HttpClient other = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        send(other, "GET", "/site/settings", null);
        HttpResponse<String> login = send(other, "POST", "/auth/login",
                "{\"identifier\":\"" + email + "\",\"password\":\"MotDePasse123\"}");
        assertThat(login.statusCode()).isEqualTo(200);
    }
}
