package com.projet.site.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointment_status_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppointmentStatusEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private AppointmentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private AppointmentStatus newStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AppointmentStatusEvent(UUID appointmentId, AppointmentStatus previousStatus, AppointmentStatus newStatus) {
        this.appointmentId = appointmentId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.createdAt = Instant.now();
    }
}
