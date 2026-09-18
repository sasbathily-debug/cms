package com.projet.site.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Exception ponctuelle à la grille récurrente : jour fermé (férié, congé) ou horaires
 * spéciaux pour une date donnée. Prime sur les {@link AvailabilityRule} de ce jour-là.
 */
@Entity
@Table(name = "schedule_exceptions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleException {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private boolean closed;

    @Column(name = "custom_start_time")
    private LocalTime customStartTime;

    @Column(name = "custom_end_time")
    private LocalTime customEndTime;

    @Column(name = "slot_duration_minutes")
    private Integer slotDurationMinutes;

    private String note;

    public ScheduleException(LocalDate date, boolean closed, LocalTime customStartTime, LocalTime customEndTime,
                              Integer slotDurationMinutes, String note) {
        this.date = date;
        this.closed = closed;
        this.customStartTime = customStartTime;
        this.customEndTime = customEndTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.note = note;
    }
}
