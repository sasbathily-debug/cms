package com.projet.site.service;

import com.projet.integration.notification.AppointmentNotice;
import com.projet.integration.notification.AppointmentNotifier;
import com.projet.user.dto.UserDto;
import com.projet.user.exception.UserNotFoundException;
import com.projet.user.service.UserService;
import com.projet.site.domain.Appointment;
import com.projet.site.domain.AppointmentStatus;
import com.projet.site.domain.AppointmentStatusEvent;
import com.projet.site.dto.AppointmentCountsDto;
import com.projet.site.dto.AppointmentDto;
import com.projet.site.dto.AppointmentRequest;
import com.projet.site.dto.AppointmentStatsDto;
import com.projet.site.dto.AppointmentStatusEventDto;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.exception.SlotAlreadyBookedException;
import com.projet.site.mapper.AppointmentMapper;
import com.projet.site.mapper.AppointmentStatusEventMapper;
import com.projet.site.repository.AppointmentRepository;
import com.projet.site.repository.AppointmentSpecifications;
import com.projet.site.repository.AppointmentStatusEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusEventRepository statusEventRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentStatusEventMapper statusEventMapper;
    private final AppointmentNotifier notifier;
    private final UserService userService;
    private final SiteSettingsService siteSettingsService;
    private final String frontendUrl;

    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            AppointmentStatusEventRepository statusEventRepository,
            AppointmentMapper appointmentMapper,
            AppointmentStatusEventMapper statusEventMapper,
            AppointmentNotifier notifier,
            UserService userService,
            SiteSettingsService siteSettingsService,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.appointmentRepository = appointmentRepository;
        this.statusEventRepository = statusEventRepository;
        this.appointmentMapper = appointmentMapper;
        this.statusEventMapper = statusEventMapper;
        this.notifier = notifier;
        this.userService = userService;
        this.siteSettingsService = siteSettingsService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public AppointmentDto create(AppointmentRequest request, UUID userId) {
        return book(request, userId, true);
    }

    @Override
    public AppointmentDto createGuest(AppointmentRequest request, boolean contactVerified) {
        return book(request, null, contactVerified);
    }

    private AppointmentDto book(AppointmentRequest request, UUID userId, boolean contactVerified) {
        Appointment appointment = new Appointment(
                request.fullName(),
                request.phone(),
                request.email(),
                request.reason(),
                request.preferredDate(),
                request.preferredTime(),
                request.message(),
                userId
        );
        appointment.setContactVerified(contactVerified);
        Appointment saved;
        try {
            saved = appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new SlotAlreadyBookedException();
        }

        statusEventRepository.save(new AppointmentStatusEvent(saved.getId(), null, saved.getStatus()));
        sendConfirmedNotifications(saved);
        return appointmentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentDto> findAll(AppointmentStatus status, LocalDate date, LocalDate from, LocalDate to, String search, boolean pastDue, Pageable pageable) {
        Specification<Appointment> spec = AppointmentSpecifications.filter(status, date, from, to, search, pastDue);
        return appointmentRepository.findAll(spec, pageable).map(appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentDto> findAllForUser(UUID userId, Pageable pageable) {
        return appointmentRepository.findByUserId(userId, pageable).map(appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentCountsDto counts(LocalDate date, LocalDate from, LocalDate to, String search) {
        long all = appointmentRepository.count(AppointmentSpecifications.filter(null, date, from, to, search, false));
        long confirmed = appointmentRepository.count(AppointmentSpecifications.filter(AppointmentStatus.CONFIRMED, date, from, to, search, false));
        long cancelled = appointmentRepository.count(AppointmentSpecifications.filter(AppointmentStatus.CANCELLED, date, from, to, search, false));
        long completed = appointmentRepository.count(AppointmentSpecifications.filter(AppointmentStatus.COMPLETED, date, from, to, search, false));
        long noShow = appointmentRepository.count(AppointmentSpecifications.filter(AppointmentStatus.NO_SHOW, date, from, to, search, false));
        long pastDue = appointmentRepository.count(AppointmentSpecifications.filter(null, date, from, to, search, true));
        return new AppointmentCountsDto(all, confirmed, cancelled, completed, noShow, pastDue);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentStatsDto stats() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        long todayCount = appointmentRepository.count(
                AppointmentSpecifications.filter(AppointmentStatus.CONFIRMED, today, null, null, null, false));
        long weekCount = appointmentRepository.count(
                AppointmentSpecifications.filter(AppointmentStatus.CONFIRMED, null, weekStart, weekEnd, null, false));
        return new AppointmentStatsDto(todayCount, weekCount);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv(AppointmentStatus status, LocalDate date, LocalDate from, LocalDate to, String search, boolean pastDue) {
        Specification<Appointment> spec = AppointmentSpecifications.filter(status, date, from, to, search, pastDue);
        List<Appointment> appointments = appointmentRepository.findAll(spec);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // UTF-8 BOM for Excel
        StringBuilder csv = new StringBuilder();
        csv.append("Nom,Téléphone,Email,Motif,Date souhaitée,Heure,Statut,Message,Créé le\n");
        for (Appointment a : appointments) {
            csv.append(csvField(a.getFullName())).append(',')
                    .append(csvField(a.getPhone())).append(',')
                    .append(csvField(a.getEmail())).append(',')
                    .append(csvField(a.getReason())).append(',')
                    .append(csvField(a.getPreferredDate() != null ? a.getPreferredDate().toString() : "")).append(',')
                    .append(csvField(a.getPreferredTime() != null ? a.getPreferredTime().toString() : "")).append(',')
                    .append(csvField(statusLabel(a.getStatus()))).append(',')
                    .append(csvField(a.getMessage())).append(',')
                    .append(csvField(a.getCreatedAt() != null ? a.getCreatedAt().toString() : ""))
                    .append('\n');
        }
        out.writeBytes(csv.toString().getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private static String statusLabel(AppointmentStatus status) {
        return switch (status) {
            case CONFIRMED -> "Confirmé";
            case CANCELLED -> "Annulé";
            case COMPLETED -> "Terminé";
            case NO_SHOW -> "Absent";
        };
    }

    private static String csvField(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public AppointmentDto updateStatus(UUID id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous introuvable : " + id));

        AppointmentStatus previous = appointment.getStatus();
        if (previous == status) {
            return appointmentMapper.toDto(appointment);
        }

        appointment.setStatus(status);
        Appointment saved = appointmentRepository.save(appointment);
        statusEventRepository.save(new AppointmentStatusEvent(saved.getId(), previous, status));
        sendStatusChangeEmail(saved);
        return appointmentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentStatusEventDto> findHistory(UUID appointmentId, UUID requesterId, boolean isAdmin) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous introuvable : " + appointmentId));

        boolean isOwner = requesterId != null && requesterId.equals(appointment.getUserId());
        if (!isAdmin && !isOwner) {
            throw new ResourceNotFoundException("Rendez-vous introuvable : " + appointmentId);
        }

        return statusEventRepository.findByAppointmentIdOrderByCreatedAtAsc(appointmentId)
                .stream()
                .map(statusEventMapper::toDto)
                .toList();
    }

    @Override
    public void sendDueReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> due = appointmentRepository.findByPreferredDateAndStatusAndReminderSentFalse(
                tomorrow, AppointmentStatus.CONFIRMED);

        for (Appointment appointment : due) {
            try {
                sendReminderNotifications(appointment);
            } catch (RuntimeException e) {
                log.warn("Échec de l'envoi du rappel pour le rendez-vous {} : {}", appointment.getId(), e.getMessage());
            }
            appointment.setReminderSent(true);
            appointmentRepository.save(appointment);
        }
    }

    private void sendReminderNotifications(Appointment appointment) {
        boolean wantsEmail = true;
        boolean wantsPhoneMessage = true;

        if (appointment.getUserId() != null) {
            try {
                UserDto user = userService.getById(appointment.getUserId());
                wantsEmail = user.emailReminders();
                wantsPhoneMessage = user.smsReminders();
            } catch (UserNotFoundException e) {
                log.warn("Rappel : utilisateur {} introuvable, envoi par défaut", appointment.getUserId());
            }
        }

        notifier.notify(notice(appointment, AppointmentNotice.Kind.REMINDER, wantsEmail, wantsPhoneMessage));
    }

    private void sendStatusChangeEmail(Appointment appointment) {
        switch (appointment.getStatus()) {
            case CONFIRMED -> sendConfirmedNotifications(appointment);
            case CANCELLED -> notifier.notify(notice(appointment, AppointmentNotice.Kind.CANCELLED, true, true));
            default -> {
                // Les autres changements de statut ne préviennent pas le patient.
            }
        }
    }

    private void sendConfirmedNotifications(Appointment appointment) {
        notifier.notify(notice(appointment, AppointmentNotice.Kind.CONFIRMED, true, true));
    }

    private AppointmentNotice notice(Appointment appointment, AppointmentNotice.Kind kind, boolean wantsEmail, boolean wantsPhoneMessage) {
        String cabinetPhone = null;
        String cabinetWhatsapp = null;
        try {
            var settings = siteSettingsService.get();
            cabinetPhone = settings.phone();
            cabinetWhatsapp = settings.whatsappNumber();
        } catch (RuntimeException e) {
            log.warn("Réglages du site indisponibles pour prévenir le patient : {}", e.getMessage());
        }
        return new AppointmentNotice(
                kind,
                appointment.getFullName(),
                appointment.getPhone(),
                appointment.getEmail(),
                dateInfo(appointment),
                appointment.getUserId() == null,
                cabinetPhone,
                cabinetWhatsapp,
                frontendUrl,
                wantsEmail,
                wantsPhoneMessage);
    }

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH);

    /** « pour le mardi 10 novembre à 9h00 » (et non « 2026-11-10 à 09:00:00 »). */
    static String dateInfo(Appointment appointment) {
        if (appointment.getPreferredDate() == null) {
            return "";
        }
        String hour = appointment.getPreferredTime() == null
                ? ""
                : " à " + appointment.getPreferredTime().getHour() + "h"
                        + String.format("%02d", appointment.getPreferredTime().getMinute());
        return " pour le " + DAY_FORMAT.format(appointment.getPreferredDate()) + hour;
    }
}
