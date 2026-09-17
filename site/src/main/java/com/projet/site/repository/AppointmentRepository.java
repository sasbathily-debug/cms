package com.projet.site.repository;

import com.projet.site.domain.Appointment;
import com.projet.site.domain.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
}
