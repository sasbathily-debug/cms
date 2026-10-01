package com.projet.site.service;

import com.projet.integration.mail.EmailContent;
import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.integration.mail.EmailTemplates;
import com.projet.integration.notification.WhatsAppNotifier;
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
    private final EmailSender emailSender;
    private final WhatsAppNotifier whatsAppNotifier;
    private final UserService userService;
    private final String frontendUrl;

    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            AppointmentStatusEventRepository statusEventRepository,
            AppointmentMapper appointmentMapper,
            AppointmentStatusEventMapper statusEventMapper,
            EmailSender emailSender,
            WhatsAppNotifier whatsAppNotifier,
            UserService userService,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.appointmentRepository = appointmentRepository;
        this.statusEventRepository = statusEventRepository;
        this.appointmentMapper = appointmentMapper;
        this.statusEventMapper = statusEventMapper;
        this.emailSender = emailSender;
        this.whatsAppNotifier = whatsAppNotifier;
        this.userService = userService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public AppointmentDto create(AppointmentRequest request, UUID userId) {
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
        boolean sendEmail = true;
        boolean sendWhatsApp = true;

        if (appointment.getUserId() != null) {
            try {
                UserDto user = userService.getById(appointment.getUserId());
                sendEmail = user.emailReminders();
                sendWhatsApp = user.smsReminders();
            } catch (UserNotFoundException e) {
                log.warn("Rappel : utilisateur {} introuvable, envoi par défaut", appointment.getUserId());
            }
        }

        String dateInfo = appointment.getPreferredDate() != null
                ? " pour le " + appointment.getPreferredDate() + " à " + appointment.getPreferredTime()
                : "";
        String ctaUrl = frontendUrl + "/client";

        if (sendEmail && StringUtils.hasText(appointment.getEmail())) {
            EmailContent content = EmailTemplates.appointmentReminder(appointment.getFullName(), dateInfo, ctaUrl);
            trySend(appointment.getEmail(), content);
        }

        if (sendWhatsApp) {
            try {
                whatsAppNotifier.sendAppointmentReminder(appointment.getPhone(), appointment.getFullName(), dateInfo, ctaUrl);
            } catch (RuntimeException e) {
                log.warn("Échec de l'envoi WhatsApp à {} : {}", appointment.getPhone(), e.getMessage());
            }
        }
    }

    private void sendStatusChangeEmail(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            sendConfirmedNotifications(appointment);
            return;
        }
        if (!StringUtils.hasText(appointment.getEmail())) {
            return;
        }

        EmailContent content = switch (appointment.getStatus()) {
            case CANCELLED -> EmailTemplates.appointmentCancelled(appointment.getFullName(), frontendUrl + "/#rdv");
            default -> null;
        };

        if (content != null) {
            trySend(appointment.getEmail(), content);
        }
    }

    private void sendConfirmedNotifications(Appointment appointment) {
        String dateInfo = appointment.getPreferredDate() != null
                ? " pour le " + appointment.getPreferredDate() + " à " + appointment.getPreferredTime()
                : "";
        String ctaUrl = frontendUrl + "/client";

        if (StringUtils.hasText(appointment.getEmail())) {
            EmailContent content = EmailTemplates.appointmentConfirmed(appointment.getFullName(), dateInfo, ctaUrl);
            trySend(appointment.getEmail(), content);
        }

        try {
            whatsAppNotifier.sendAppointmentConfirmed(appointment.getPhone(), appointment.getFullName(), dateInfo, ctaUrl);
        } catch (RuntimeException e) {
            log.warn("Échec de l'envoi WhatsApp à {} : {}", appointment.getPhone(), e.getMessage());
        }
    }

    private void trySend(String to, EmailContent content) {
        try {
            emailSender.send(new EmailMessage(to, content.subject(), content.text(), content.html()));
        } catch (RuntimeException e) {
            log.warn("Échec de l'envoi d'email à {} : {}", to, e.getMessage());
        }
    }
}
