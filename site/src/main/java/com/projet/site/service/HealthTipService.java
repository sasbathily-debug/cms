package com.projet.site.service;

import com.projet.site.dto.HealthTipDto;
import com.projet.site.dto.HealthTipRequest;

import java.util.List;
import java.util.UUID;

public interface HealthTipService {

    /** Articles publiés, pour le site public. */
    List<HealthTipDto> findAll();

    /** Tous les articles, brouillons compris, pour l'admin. */
    List<HealthTipDto> findAllForAdmin();

    HealthTipDto create(HealthTipRequest request);

    HealthTipDto update(UUID id, HealthTipRequest request);

    void delete(UUID id);
}
