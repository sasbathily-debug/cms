package com.projet.site.service;

import com.projet.site.dto.CareServiceDto;
import com.projet.site.dto.CareServiceRequest;

import java.util.List;
import java.util.UUID;

public interface ServiceCatalogService {

    List<CareServiceDto> findAll();

    CareServiceDto create(CareServiceRequest request);

    CareServiceDto update(UUID id, CareServiceRequest request);

    void delete(UUID id);
}
