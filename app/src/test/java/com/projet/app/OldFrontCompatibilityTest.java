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
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ce que l'ancien site appelle, avec les formats qu'il envoie : pages publiques, créneaux, réservation,
 * liste des rendez-vous côté admin. Garde-fou avant de déployer cette branche sous l'ancien front.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OldFrontCompatibilityTest {

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

    private HttpClient browser() throws Exception {
        HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        send(client, "GET", "/site/settings", null);
        return client;
    }

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
    void lesPagesPubliquesDeLAncienSiteRepondent() throws Exception {
        HttpClient visitor = browser();
        LocalDate from = LocalDate.now();
        for (String path : new String[]{
                "/site/services", "/site/faq", "/site/settings", "/site/health-tips", "/site/hero-images",
                "/site/availability?from=" + from + "&to=" + from.plusDays(30)}) {
            assertThat(send(visitor, "GET", path, null).statusCode()).as(path).isEqualTo(200);
        }
    }

    @Test
    void laReservationFonctionneAvecLesFormatsDeLAncienSite() throws Exception {
        HttpClient admin = browser();
        assertThat(send(admin, "POST", "/auth/login",
                "{\"identifier\":\"admin@projet.local\",\"password\":\"Admin123!\"}").statusCode()).isEqualTo(200);
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).plusWeeks(1);

        HttpResponse<String> rule = send(admin, "POST", "/site/availability/rules",
                "{\"dayOfWeek\":\"MONDAY\",\"startTime\":\"09:00\",\"endTime\":\"12:00\",\"slotDurationMinutes\":30}");
        assertThat(rule.statusCode()).isIn(200, 201);

        HttpClient visitor = browser();
        HttpResponse<String> slots = send(visitor, "GET",
                "/site/availability?from=" + monday + "&to=" + monday, null);
        assertThat(slots.statusCode()).isEqualTo(200);
        assertThat(slots.body()).contains("\"time\":\"09:00");

        String booking = "{\"fullName\":\"Awa Diop\",\"phone\":\"221771234567\",\"email\":\"awa@example.com\","
                + "\"reason\":\"Première consultation\",\"preferredDate\":\"" + monday
                + "\",\"preferredTime\":\"09:00\",\"message\":null}";
        HttpResponse<String> booked = send(visitor, "POST", "/appointments", booking);
        assertThat(booked.statusCode()).isEqualTo(201);
        assertThat(booked.body()).contains("CONFIRMED");

        assertThat(send(visitor, "POST", "/appointments", booking).statusCode()).isEqualTo(409);
        assertThat(send(visitor, "GET", "/site/availability?from=" + monday + "&to=" + monday, null).body())
                .doesNotContain("\"time\":\"09:00");

        HttpResponse<String> list = send(admin, "GET", "/appointments", null);
        assertThat(list.statusCode()).isEqualTo(200);
        assertThat(list.body()).contains("Awa Diop");
    }
}
