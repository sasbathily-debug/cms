package com.projet.site.service;

import com.projet.site.domain.HealthTip;
import com.projet.site.dto.HealthTipDto;
import com.projet.site.dto.HealthTipRequest;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.mapper.HealthTipMapper;
import com.projet.site.repository.HealthTipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HealthTipServiceImpl implements HealthTipService {

    private final HealthTipRepository healthTipRepository;
    private final HealthTipMapper healthTipMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HealthTipDto> findAll() {
        return healthTipRepository.findAllByPublishedTrueOrderByDisplayOrderAsc().stream()
                .map(healthTipMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthTipDto> findAllForAdmin() {
        return healthTipRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(healthTipMapper::toDto)
                .toList();
    }

    @Override
    public HealthTipDto create(HealthTipRequest request) {
        HealthTip healthTip = new HealthTip(request.title(), request.body(), request.icon(), request.displayOrder());
        apply(healthTip, request);
        return healthTipMapper.toDto(healthTipRepository.save(healthTip));
    }

    @Override
    public HealthTipDto update(UUID id, HealthTipRequest request) {
        HealthTip healthTip = healthTipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conseil introuvable : " + id));
        healthTip.setTitle(request.title());
        healthTip.setBody(request.body());
        healthTip.setIcon(request.icon());
        healthTip.setDisplayOrder(request.displayOrder());
        apply(healthTip, request);
        return healthTipMapper.toDto(healthTipRepository.save(healthTip));
    }

    private static void apply(HealthTip healthTip, HealthTipRequest request) {
        healthTip.setCategory(blankToNull(request.category()));
        healthTip.setReadingMinutes(request.readingMinutes());
        healthTip.setPhotoUrl(blankToNull(request.photoUrl()));
        healthTip.setPublished(request.published() == null || request.published());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public void delete(UUID id) {
        if (!healthTipRepository.existsById(id)) {
            throw new ResourceNotFoundException("Conseil introuvable : " + id);
        }
        healthTipRepository.deleteById(id);
    }
}
