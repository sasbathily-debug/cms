package com.projet.site.repository;

import com.projet.site.domain.Appointment;
import com.projet.site.domain.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;

public final class AppointmentSpecifications {

    private AppointmentSpecifications() {
    }

    public static Specification<Appointment> hasStatus(AppointmentStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Appointment> onDate(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.equal(root.get("preferredDate"), date);
    }

    public static Specification<Appointment> dateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("preferredDate"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("preferredDate"), from);
            }
            if (to != null) {
                return cb.lessThanOrEqualTo(root.get("preferredDate"), to);
            }
            return null;
        };
    }

    public static Specification<Appointment> isPastDue() {
        return (root, query, cb) -> {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            return cb.and(
                    cb.equal(root.get("status"), AppointmentStatus.CONFIRMED),
                    cb.isNotNull(root.get("preferredDate")),
                    cb.isNotNull(root.get("preferredTime")),
                    cb.or(
                            cb.lessThan(root.get("preferredDate"), today),
                            cb.and(
                                    cb.equal(root.get("preferredDate"), today),
                                    cb.lessThan(root.get("preferredTime"), now)
                            )
                    )
            );
        };
    }

    public static Specification<Appointment> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return null;
            }
            String term = search.trim();
            String likeTerm = "%" + term.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), likeTerm),
                    cb.like(root.get("phone"), "%" + term + "%"),
                    cb.like(cb.lower(cb.coalesce(root.get("email"), "")), likeTerm)
            );
        };
    }

    public static Specification<Appointment> filter(AppointmentStatus status, LocalDate date, LocalDate from, LocalDate to, String search, boolean pastDue) {
        return Specification.allOf(
                pastDue ? isPastDue() : hasStatus(status),
                date != null ? onDate(date) : dateBetween(from, to),
                matchesSearch(search)
        );
    }
}
