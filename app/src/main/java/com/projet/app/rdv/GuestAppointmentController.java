package com.projet.app.rdv;

import com.projet.integration.notification.BookingCodeMailer;
import com.projet.security.config.OtpProperties;
import com.projet.security.exception.InvalidOtpException;
import com.projet.security.exception.OtpRateLimitException;
import com.projet.security.service.OtpService;
import com.projet.site.dto.AppointmentDto;
import com.projet.site.dto.AppointmentRequest;
import com.projet.site.service.AppointmentService;
import com.projet.site.service.GuestBookingLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prendre rendez-vous sans créer de compte. Un compte devient facultatif : le patient laisse son nom et son téléphone.
 * Contre les faux rendez-vous : champ piège, plafonds par IP et par téléphone, et, quand une adresse e-mail est donnée,
 * un code à 6 chiffres reçu à cette adresse (même mécanisme que l'inscription : 5 essais, durée limitée).
 */
@Slf4j
@RestController
@RequestMapping("/appointments/guest")
@RequiredArgsConstructor
public class GuestAppointmentController {

    private static final String TROP_DE_DEMANDES =
            "Trop de demandes. Réessayez plus tard ou écrivez-nous sur WhatsApp.";

    private final OtpService otpService;
    private final OtpProperties otpProperties;
    private final BookingCodeMailer bookingCodeMailer;
    private final AppointmentService appointmentService;
    private final GuestBookingLimiter limiter;

    /** Envoie le code de confirmation à l'adresse e-mail du patient. */
    @PostMapping("/code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendCode(@Valid @RequestBody GuestCodeRequest request, HttpServletRequest http) {
        if (StringUtils.hasText(request.website())) {
            log.info("Code de rendez-vous ignoré (champ piège rempli) depuis {}", clientIp(http));
            return;
        }
        String email = normalise(request.email());
        // Un plafond par IP et par adresse, comme pour une réservation : on ne peut pas inonder une boîte e-mail.
        if (!limiter.tryAcquire(clientIp(http), email)) {
            throw new OtpRateLimitException(TROP_DE_DEMANDES);
        }
        String code = otpService.generateForBooking(email);
        bookingCodeMailer.send(email, request.fullName().strip(), code, (int) otpProperties.ttlMinutes());
    }

    /** Enregistre le rendez-vous (créneau réservé tout de suite) et renvoie sa fiche. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDto book(@Valid @RequestBody GuestAppointmentRequest request, HttpServletRequest http) {
        if (StringUtils.hasText(request.website())) {
            // Piège à robots : on refuse sans détail.
            log.info("Rendez-vous ignoré (champ piège rempli) depuis {}", clientIp(http));
            throw new InvalidOtpException("Demande invalide.");
        }
        boolean withEmail = StringUtils.hasText(request.email());
        boolean verified = false;
        if (withEmail) {
            String email = normalise(request.email());
            if (!StringUtils.hasText(request.code()) || !otpService.verifyForBooking(email, request.code().strip())) {
                throw new InvalidOtpException("Code incorrect ou expiré. Demandez un nouveau code.");
            }
            verified = true;
        }
        // Le plafond ne compte que les réservations aboutissant : le code, lui, a déjà été limité à sa demande.
        if (!limiter.tryAcquire(clientIp(http), request.phone())) {
            throw new OtpRateLimitException(TROP_DE_DEMANDES);
        }
        AppointmentRequest appointment = new AppointmentRequest(
                request.fullName().strip(),
                request.phone().strip(),
                withEmail ? normalise(request.email()) : null,
                request.reason().strip(),
                request.preferredDate(),
                request.preferredTime(),
                request.message());
        return appointmentService.createGuest(appointment, verified);
    }

    private static String normalise(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    /** Derrière Vercel puis Render, la vraie adresse est le premier élément de X-Forwarded-For. */
    private static String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].strip();
        }
        return http.getRemoteAddr();
    }
}
