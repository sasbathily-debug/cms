package com.projet.site.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;

/** Horaires d'un jour de la semaine (1 = lundi ... 7 = dimanche) : une plage, ou fermé. */
@Entity
@Table(name = "opening_hours")
@Getter
@Setter
@NoArgsConstructor
public class OpeningHour {

    /** La colonne est un SMALLINT (migration V21) : on le dit à Hibernate, sinon la validation du schéma échoue. */
    @Id
    @Column(name = "day_of_week")
    @JdbcTypeCode(SqlTypes.SMALLINT)
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
