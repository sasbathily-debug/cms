package com.projet.site.service;

import com.projet.site.dto.CreateHeroImageRequest;
import com.projet.site.dto.HeroImageDto;
import com.projet.site.dto.ReorderHeroImagesRequest;

import java.util.List;
import java.util.UUID;

public interface HeroImageService {

    List<HeroImageDto> findAll();

    HeroImageDto create(CreateHeroImageRequest request);

    void reorder(ReorderHeroImagesRequest request);

    void delete(UUID id);
}
