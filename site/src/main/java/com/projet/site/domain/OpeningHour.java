package com.projet.site.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/** Horaires d'un jour de la semaine (1 = lundi ... 7 = dimanche) : une plage, ou fermé. */
@Entity
@Table(name = "opening_hours")
@Getter
@Setter
@NoArgsConstructor
public class OpeningHour {

    @Id
    @Column(name = "day_of_week")
    private int dayOfWeek;

    private LocalTime opens;

    private LocalTime closes;

    @Column(nullable = false)
    private boolean closed;

    public OpeningHour(int dayOfWeek, LocalTime opens, LocalTime closes, boolean closed) {
        this.dayOfWeek = dayOfWeek;
        this.opens = opens;
        this.closes = closes;
        this.closed = closed;
    }
}
