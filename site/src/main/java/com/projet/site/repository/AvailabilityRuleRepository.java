package com.projet.site.repository;

import com.projet.site.domain.AvailabilityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AvailabilityRuleRepository extends JpaRepository<AvailabilityRule, UUID> {

    List<AvailabilityRule> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<AvailabilityRule> findAllByOrderByDayOfWeekAscStartTimeAsc();

    boolean existsByDayOfWeekAndStartTimeAndEndTime(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);

    boolean existsByDayOfWeekAndStartTimeAndEndTimeAndIdNot(
            DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, UUID id);
}
