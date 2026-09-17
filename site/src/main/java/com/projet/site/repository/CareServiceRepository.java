package com.projet.site.repository;

import com.projet.site.domain.CareService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CareServiceRepository extends JpaRepository<CareService, UUID> {

    List<CareService> findAllByOrderByDisplayOrderAsc();
}
