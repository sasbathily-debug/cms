package com.projet.site.service;

import com.projet.site.domain.CareService;
import com.projet.site.dto.CareServiceDto;
import com.projet.site.dto.CareServiceRequest;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.mapper.CareServiceMapper;
import com.projet.site.repository.CareServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceCatalogServiceImpl implements ServiceCatalogService {

    private final CareServiceRepository careServiceRepository;
    private final CareServiceMapper careServiceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CareServiceDto> findAll() {
        return careServiceRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(careServiceMapper::toDto)
                .toList();
    }

    @Override
    public CareServiceDto create(CareServiceRequest request) {
        CareService careService = new CareService(request.title(), request.description(), request.icon(), request.displayOrder());
        return careServiceMapper.toDto(careServiceRepository.save(careService));
    }

    @Override
    public CareServiceDto update(UUID id, CareServiceRequest request) {
        CareService careService = careServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service introuvable : " + id));
        careService.setTitle(request.title());
        careService.setDescription(request.description());
        careService.setIcon(request.icon());
        careService.setDisplayOrder(request.displayOrder());
        return careServiceMapper.toDto(careServiceRepository.save(careService));
    }

    @Override
    public void delete(UUID id) {
        if (!careServiceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service introuvable : " + id);
        }
        careServiceRepository.deleteById(id);
    }
}
