package com.projet.site.service;

import com.projet.site.dto.FaqItemDto;
import com.projet.site.dto.FaqItemRequest;

import java.util.List;
import java.util.UUID;

public interface FaqItemService {

    List<FaqItemDto> findAll();

    FaqItemDto create(FaqItemRequest request);

    FaqItemDto update(UUID id, FaqItemRequest request);

    void delete(UUID id);
}
