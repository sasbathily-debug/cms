package com.projet.site.repository;

import com.projet.site.domain.AppointmentStatusEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentStatusEventRepository extends JpaRepository<AppointmentStatusEvent, UUID> {

    List<AppointmentStatusEvent> findByAppointmentIdOrderByCreatedAtAsc(UUID appointmentId);
}
