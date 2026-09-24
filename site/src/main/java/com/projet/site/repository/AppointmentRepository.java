package com.projet.site.repository;

import com.projet.site.domain.Appointment;
import com.projet.site.domain.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    Page<Appointment> findByUserId(UUID userId, Pageable pageable);

    List<Appointment> findByPreferredDateBetweenAndStatusIn(LocalDate from, LocalDate to, List<AppointmentStatus> statuses);

    List<Appointment> findByPreferredDateAndStatusAndReminderSentFalse(LocalDate preferredDate, AppointmentStatus status);
}
