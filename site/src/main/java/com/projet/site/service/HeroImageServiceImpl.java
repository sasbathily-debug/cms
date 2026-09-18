package com.projet.site.service;

import com.projet.site.domain.HeroImage;
import com.projet.site.dto.CreateHeroImageRequest;
import com.projet.site.dto.HeroImageDto;
import com.projet.site.dto.ReorderHeroImagesRequest;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.mapper.HeroImageMapper;
import com.projet.site.repository.HeroImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HeroImageServiceImpl implements HeroImageService {

    private final HeroImageRepository heroImageRepository;
    private final HeroImageMapper heroImageMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HeroImageDto> findAll() {
        return heroImageRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(heroImageMapper::toDto)
                .toList();
    }

    @Override
    public HeroImageDto create(CreateHeroImageRequest request) {
        int nextOrder = heroImageRepository.findAllByOrderByDisplayOrderAsc().stream()
                .mapToInt(HeroImage::getDisplayOrder)
                .max()
                .orElse(-1) + 1;
        HeroImage heroImage = new HeroImage(request.imageUrl(), nextOrder);
        return heroImageMapper.toDto(heroImageRepository.save(heroImage));
    }

    @Override
    public void reorder(ReorderHeroImagesRequest request) {
        Map<UUID, HeroImage> byId = heroImageRepository.findAllById(request.orderedIds()).stream()
                .collect(Collectors.toMap(HeroImage::getId, image -> image));
        if (byId.size() != request.orderedIds().size()) {
            throw new ResourceNotFoundException("Une ou plusieurs images sont introuvables");
        }
        List<UUID> orderedIds = request.orderedIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            byId.get(orderedIds.get(i)).setDisplayOrder(i);
        }
    }

    @Override
    public void delete(UUID id) {
        if (!heroImageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Image introuvable : " + id);
        }
        heroImageRepository.deleteById(id);
    }
}
