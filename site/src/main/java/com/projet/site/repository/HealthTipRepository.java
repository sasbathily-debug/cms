package com.projet.site.repository;

import com.projet.site.domain.HealthTip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HealthTipRepository extends JpaRepository<HealthTip, UUID> {

    List<HealthTip> findAllByOrderByDisplayOrderAsc();

    List<HealthTip> findAllByPublishedTrueOrderByDisplayOrderAsc();
}
