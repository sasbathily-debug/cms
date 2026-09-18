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
        return healthTipRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(healthTipMapper::toDto)
                .toList();
    }

    @Override
    public HealthTipDto create(HealthTipRequest request) {
        HealthTip healthTip = new HealthTip(request.title(), request.body(), request.icon(), request.displayOrder());
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
        return healthTipMapper.toDto(healthTipRepository.save(healthTip));
    }

    @Override
    public void delete(UUID id) {
        if (!healthTipRepository.existsById(id)) {
            throw new ResourceNotFoundException("Conseil introuvable : " + id);
        }
        healthTipRepository.deleteById(id);
    }
}
