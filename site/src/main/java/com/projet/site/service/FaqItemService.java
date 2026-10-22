package com.projet.site.service;

import com.projet.site.dto.FaqItemDto;
import com.projet.site.dto.FaqItemRequest;

import java.util.List;
import java.util.UUID;

public interface FaqItemService {

    /** Questions publiées, pour le site public. */
    List<FaqItemDto> findAll();

    /** Toutes les questions, brouillons compris, pour l'admin. */
    List<FaqItemDto> findAllForAdmin();

    FaqItemDto create(FaqItemRequest request);

    FaqItemDto update(UUID id, FaqItemRequest request);

    void delete(UUID id);
}
