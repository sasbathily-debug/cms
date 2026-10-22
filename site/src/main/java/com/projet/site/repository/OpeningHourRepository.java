package com.projet.site.repository;

import com.projet.site.domain.OpeningHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpeningHourRepository extends JpaRepository<OpeningHour, Integer> {

    List<OpeningHour> findAllByOrderByDayOfWeekAsc();
}
