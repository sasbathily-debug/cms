package com.projet.app;

import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.site.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.io.ByteArrayOutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Les parcours qui suivent la réservation, tels que l'ancien site les utilise : suivi d'un rendez-vous
 * (statuts, historique, e-mails, exports), documents du patient, envoi de fichiers, profil, mot de passe
 * et rappels de la veille. Deux patients, pour vérifier que chacun ne voit que ses données.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PatientAndAdminFlowsTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static Path uploads;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws Exception {
        uploads = Files.createTempDirectory("uploads-test");
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("app.storage.local.base-dir", () -> uploads.toString());
    }

    @LocalServerPort
    int port;

    @MockitoBean
    EmailSender emailSender;

    @Autowired
    AppointmentService appointmentService;

    // ---------- Aides ----------

    private HttpResponse<String> send(HttpClient client, String method, String path, String json) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        csrf(client, builder);
        HttpRequest.BodyPublisher body = json == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json);
        return client.send(builder.method(method, body).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<byte[]> upload(HttpClient client, String filename, byte[] content) throws Exception {
        String boundary = "----test" + System.nanoTime();
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + filename
                + "\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(content);
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/files"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary);
        csrf(client, builder);
        return client.send(builder.POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build(),
                HttpResponse.BodyHandlers.ofByteArray());
    }

    private void csrf(HttpClient client, HttpRequest.Builder builder) {
        ((CookieManager) client.cookieHandler().orElseThrow()).getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("XSRF-TOKEN"))
                .map(HttpCookie::getValue)
                .findFirst()
                .ifPresent(token -> builder.header("X-XSRF-TOKEN", token));
    }

    private HttpClient browser() throws Exception {
        HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        send(client, "GET", "/site/settings", null);
        return client;
    }

    private HttpClient admin() throws Exception {
        HttpClient client = browser();
        assertThat(send(client, "POST", "/auth/login",
                "{\"identifier\":\"admin@projet.local\",\"password\":\"Admin123!\"}").statusCode()).isEqualTo(200);
        return client;
    }

    /** Crée un patient par le parcours réel (e-mail, code, mot de passe) et le laisse connecté. */
    private HttpClient patient(String email, String phone) throws Exception {
        HttpClient client = browser();
        clearInvocations(emailSender);
        assertThat(send(client, "POST", "/auth/register",
                "{\"email\":\"" + email + "\",\"firstName\":\"Patient\",\"lastName\":\"Test\",\"phone\":\"" + phone + "\"}")
                .statusCode()).isEqualTo(202);
        String code = lastCodeSentTo(email);
        assertThat(send(client, "POST", "/auth/verify-otp",
                "{\"email\":\"" + email + "\",\"otp\":\"" + code + "\"}").statusCode()).isEqualTo(200);
        assertThat(send(client, "POST", "/auth/complete-registration",
                "{\"email\":\"" + email + "\",\"password\":\"MotDePasse123\"}").statusCode()).isEqualTo(200);
        return client;
    }

    private String lastCodeSentTo(String email) {
        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, atLeastOnce()).send(sent.capture());
        List<EmailMessage> mails = sent.getAllValues().stream().filter(m -> email.equals(m.to())).toList();
        Matcher matcher = Pattern.compile("\\b(\\d{6})\\b").matcher(mails.get(mails.size() - 1).text());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private static String grab(String json, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(json);
        assertThat(matcher.find()).as("%s dans %s", regex, json).isTrue();
        return matcher.group(1);
    }

    private void openSlots(HttpClient admin, DayOfWeek day) throws Exception {
        HttpResponse<String> rule = send(admin, "POST", "/site/availability/rules",
                "{\"dayOfWeek\":\"" + day + "\",\"startTime\":\"09:00\",\"endTime\":\"12:00\",\"slotDurationMinutes\":30}");
        assertThat(rule.statusCode()).isIn(200, 201, 409);
    }

    private String book(HttpClient client, String email, LocalDate date, String time) throws Exception {
        HttpResponse<String> booked = send(client, "POST", "/appointments",
                "{\"fullName\":\"Patient Test\",\"phone\":\"221771111111\",\"email\":\"" + email
                        + "\",\"reason\":\"Suivi\",\"preferredDate\":\"" + date + "\",\"preferredTime\":\"" + time
                        + "\",\"message\":null}");
        assertThat(booked.statusCode()).isEqualTo(201);
        return grab(booked.body(), "\"id\":\"([^\"]+)\"");
    }

    // ---------- Scénarios ----------

    @Test
    void leSuiviDUnRendezVousChangeDeStatutEtGardeSonHistorique() throws Exception {
        HttpClient admin = admin();
        openSlots(admin, DayOfWeek.TUESDAY);
        LocalDate tuesday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY)).plusWeeks(1);
        HttpClient alice = patient("alice.suivi@example.com", "221770000101");
        HttpClient bob = patient("bob.suivi@example.com", "221770000102");

        String id = book(alice, "alice.suivi@example.com", tuesday, "09:30");
        assertThat(send(alice, "GET", "/appointments/me", null).body()).contains(id);
        assertThat(send(bob, "GET", "/appointments/me", null).body()).doesNotContain(id);

        HttpResponse<String> history = send(alice, "GET", "/appointments/" + id + "/history", null);
        assertThat(history.statusCode()).isEqualTo(200);
        assertThat(history.body()).contains("CONFIRMED");
        assertThat(send(bob, "GET", "/appointments/" + id + "/history", null).statusCode()).isEqualTo(404);

        assertThat(send(alice, "PATCH", "/appointments/" + id + "/status", "{\"status\":\"CANCELLED\"}").statusCode())
                .isEqualTo(403);
        assertThat(send(admin, "PATCH", "/appointments/" + id + "/status", "{\"status\":\"N_IMPORTE_QUOI\"}")
                .statusCode()).isEqualTo(400);

        // « Terminé » ne prévient pas le patient ; seule l'annulation envoie un e-mail (comportement d'origine).
        clearInvocations(emailSender);
        HttpResponse<String> done = send(admin, "PATCH", "/appointments/" + id + "/status", "{\"status\":\"COMPLETED\"}");
        assertThat(done.statusCode()).isEqualTo(200);
        assertThat(done.body()).contains("COMPLETED");
        verify(emailSender, never()).send(any());

        // Même statut : rien de nouveau dans l'historique.
        assertThat(send(admin, "PATCH", "/appointments/" + id + "/status", "{\"status\":\"COMPLETED\"}").statusCode())
                .isEqualTo(200);
        assertThat(send(alice, "GET", "/appointments/" + id + "/history", null).body()
                .split("\"newStatus\"", -1).length - 1).isEqualTo(2);

        assertThat(send(admin, "PATCH", "/appointments/" + id + "/status", "{\"status\":\"CANCELLED\"}")
                .statusCode()).isEqualTo(200);
        ArgumentCaptor<EmailMessage> mail = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, atLeastOnce()).send(mail.capture());
        assertThat(mail.getAllValues()).anyMatch(m -> "alice.suivi@example.com".equals(m.to()));
        assertThat(send(alice, "GET", "/appointments/" + id + "/history", null).body()
                .split("\"newStatus\"", -1).length - 1).isEqualTo(3);

        assertThat(send(admin, "GET", "/appointments/counts", null).statusCode()).isEqualTo(200);
        assertThat(send(admin, "GET", "/appointments/stats", null).statusCode()).isEqualTo(200);
        HttpResponse<String> csv = send(admin, "GET", "/appointments/export", null);
        assertThat(csv.statusCode()).isEqualTo(200);
        assertThat(csv.body()).contains("Nom,Téléphone");
        assertThat(send(alice, "GET", "/appointments/counts", null).statusCode()).isEqualTo(403);
    }

    @Test
    void lesDocumentsNeSontVisiblesQueDeLeurPatient() throws Exception {
        HttpClient admin = admin();
        HttpClient carole = patient("carole.docs@example.com", "221770000201");
        HttpClient david = patient("david.docs@example.com", "221770000202");
        String caroleId = grab(send(carole, "GET", "/users/me", null).body(), "\"id\":\"([^\"]+)\"");

        String document = "{\"userId\":\"" + caroleId + "\",\"appointmentId\":null,\"fileName\":\"Ordonnance.pdf\","
                + "\"fileUrl\":\"/files/ordonnance.pdf\",\"category\":\"ORDONNANCE\"}";
        assertThat(send(carole, "POST", "/documents", document).statusCode()).isEqualTo(403);
        HttpResponse<String> created = send(admin, "POST", "/documents", document);
        assertThat(created.statusCode()).isEqualTo(201);
        String documentId = grab(created.body(), "\"id\":\"([^\"]+)\"");

        assertThat(send(carole, "GET", "/documents/me", null).body()).contains("Ordonnance.pdf");
        assertThat(send(david, "GET", "/documents/me", null).body()).doesNotContain("Ordonnance.pdf");
        assertThat(send(david, "GET", "/documents", null).statusCode()).isEqualTo(403);
        assertThat(send(admin, "GET", "/documents?userId=" + caroleId, null).body()).contains("Ordonnance.pdf");

        assertThat(send(carole, "DELETE", "/documents/" + documentId, null).statusCode()).isEqualTo(403);
        assertThat(send(admin, "DELETE", "/documents/" + documentId, null).statusCode()).isEqualTo(204);
        assertThat(send(carole, "GET", "/documents/me", null).body()).doesNotContain("Ordonnance.pdf");
    }

    @Test
    void lEnvoiDeFichiersLeProfilEtLeMotDePasseFonctionnent() throws Exception {
        HttpClient emma = patient("emma.profil@example.com", "221770000301");
        byte[] picture = new byte[]{(byte) 0x89, 'P', 'N', 'G', 1, 2, 3, 4, 5};

        HttpResponse<byte[]> uploaded = upload(emma, "photo.png", picture);
        assertThat(uploaded.statusCode()).isEqualTo(200);
        String url = grab(new String(uploaded.body(), StandardCharsets.UTF_8), "\"url\":\"([^\"]+)\"");

        String path = URI.create(url).getPath();
        HttpResponse<byte[]> downloaded = emma.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertThat(downloaded.statusCode()).isEqualTo(200);
        assertThat(downloaded.body()).isEqualTo(picture);

        HttpResponse<String> updated = send(emma, "PUT", "/users/me",
                "{\"firstName\":\"Emma\",\"lastName\":\"Profil\",\"profilePictureUrl\":\"" + url
                        + "\",\"phone\":\"221770000399\",\"dateOfBirth\":\"1990-04-12\","
                        + "\"emailReminders\":false,\"smsReminders\":true}");
        assertThat(updated.statusCode()).isEqualTo(200);
        String me = send(emma, "GET", "/users/me", null).body();
        assertThat(me).contains("\"firstName\":\"Emma\"").contains(url).contains("\"smsReminders\":true")
                .contains("\"emailReminders\":false");

        assertThat(send(emma, "PUT", "/users/me/password",
                "{\"currentPassword\":\"FAUX_MOT_DE_PASSE\",\"newPassword\":\"NouveauMotDePasse1\"}").statusCode())
                .isBetween(400, 403);
        assertThat(send(emma, "PUT", "/users/me/password",
                "{\"currentPassword\":\"MotDePasse123\",\"newPassword\":\"NouveauMotDePasse1\"}").statusCode())
                .isEqualTo(204);

        HttpClient again = browser();
        assertThat(send(again, "POST", "/auth/login",
                "{\"identifier\":\"emma.profil@example.com\",\"password\":\"NouveauMotDePasse1\"}").statusCode())
                .isEqualTo(200);
        HttpClient old = browser();
        assertThat(send(old, "POST", "/auth/login",
                "{\"identifier\":\"emma.profil@example.com\",\"password\":\"MotDePasse123\"}").statusCode())
                .isBetween(400, 401);
    }

    @Test
    void leRappelDeLaVeilleEstEnvoyeUneSeuleFois() throws Exception {
        HttpClient admin = admin();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        openSlots(admin, tomorrow.getDayOfWeek());
        HttpClient fanny = patient("fanny.rappel@example.com", "221770000401");
        book(fanny, "fanny.rappel@example.com", tomorrow, "10:00");

        clearInvocations(emailSender);
        appointmentService.sendDueReminders();
        ArgumentCaptor<EmailMessage> first = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, atLeastOnce()).send(first.capture());
        assertThat(first.getAllValues()).anyMatch(m -> "fanny.rappel@example.com".equals(m.to()));

        clearInvocations(emailSender);
        appointmentService.sendDueReminders();
        ArgumentCaptor<EmailMessage> second = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, org.mockito.Mockito.atMost(10)).send(second.capture());
        assertThat(second.getAllValues()).noneMatch(m -> "fanny.rappel@example.com".equals(m.to()));
    }
}
