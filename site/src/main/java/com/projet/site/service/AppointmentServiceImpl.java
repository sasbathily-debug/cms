package com.projet.site.service;

import com.projet.integration.mail.EmailMessage;
import com.projet.integration.mail.EmailSender;
import com.projet.site.domain.Appointment;
import com.projet.site.domain.AppointmentStatus;
import com.projet.site.domain.AppointmentStatusEvent;
import com.projet.site.dto.AppointmentDto;
import com.projet.site.dto.AppointmentRequest;
import com.projet.site.dto.AppointmentStatusEventDto;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.mapper.AppointmentMapper;
import com.projet.site.mapper.AppointmentStatusEventMapper;
import com.projet.site.repository.AppointmentRepository;
import com.projet.site.repository.AppointmentStatusEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusEventRepository statusEventRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentStatusEventMapper statusEventMapper;
    private final EmailSender emailSender;

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
        Appointment saved = appointmentRepository.save(appointment);
        statusEventRepository.save(new AppointmentStatusEvent(saved.getId(), null, saved.getStatus()));
        sendCreationEmail(saved);
        return appointmentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentDto> findAll(AppointmentStatus status, Pageable pageable) {
        Page<Appointment> page = status == null
                ? appointmentRepository.findAll(pageable)
                : appointmentRepository.findByStatus(status, pageable);
        return page.map(appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentDto> findAllForUser(UUID userId, Pageable pageable) {
        return appointmentRepository.findByUserId(userId, pageable).map(appointmentMapper::toDto);
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

    private void sendCreationEmail(Appointment appointment) {
        if (!StringUtils.hasText(appointment.getEmail())) {
            return;
        }
        trySend(appointment.getEmail(), "Votre demande de rendez-vous a bien été reçue", """
                Bonjour %s,

                Nous avons bien reçu votre demande de rendez-vous (%s). Notre équipe vous confirme un créneau sous 24h ouvrées.

                À bientôt,
                Cabinet du Dr Aissatou Fall""".formatted(appointment.getFullName(), appointment.getReason()));
    }

    private void sendStatusChangeEmail(Appointment appointment) {
        if (!StringUtils.hasText(appointment.getEmail())) {
            return;
        }

        String subject;
        String body;
        switch (appointment.getStatus()) {
            case CONFIRMED -> {
                subject = "Votre rendez-vous est confirmé";
                String dateInfo = appointment.getPreferredDate() != null
                        ? " pour le " + appointment.getPreferredDate()
                        : "";
                body = """
                        Bonjour %s,

                        Votre rendez-vous a été confirmé%s.

                        À bientôt,
                        Cabinet du Dr Aissatou Fall""".formatted(appointment.getFullName(), dateInfo);
            }
            case CANCELLED -> {
                subject = "Votre rendez-vous a été annulé";
                body = """
                        Bonjour %s,

                        Votre demande de rendez-vous a été annulée. N'hésitez pas à nous recontacter pour reprogrammer.

                        Cabinet du Dr Aissatou Fall""".formatted(appointment.getFullName());
            }
            default -> {
                return;
            }
        }

        trySend(appointment.getEmail(), subject, body);
    }

    private void trySend(String to, String subject, String body) {
        try {
            emailSender.send(new EmailMessage(to, subject, body));
        } catch (RuntimeException e) {
            log.warn("Échec de l'envoi d'email à {} : {}", to, e.getMessage());
        }
    }
}
