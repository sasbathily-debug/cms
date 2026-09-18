package com.projet.site.repository;

import com.projet.site.domain.ScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, UUID> {

    List<ScheduleException> findByDateBetween(LocalDate from, LocalDate to);

    Optional<ScheduleException> findByDate(LocalDate date);

    List<ScheduleException> findAllByOrderByDateAsc();
}
