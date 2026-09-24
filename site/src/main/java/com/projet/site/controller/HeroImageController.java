package com.projet.site.controller;

import com.projet.site.dto.CreateHeroImageRequest;
import com.projet.site.dto.HeroImageDto;
import com.projet.site.dto.ReorderHeroImagesRequest;
import com.projet.site.dto.UpdateHeroImageRequest;
import com.projet.site.service.HeroImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/site/hero-images")
@RequiredArgsConstructor
public class HeroImageController {

    private final HeroImageService heroImageService;

    @GetMapping
    public List<HeroImageDto> findAll() {
        return heroImageService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HeroImageDto create(@Valid @RequestBody CreateHeroImageRequest request) {
        return heroImageService.create(request);
    }

    @PutMapping("/{id}")
    public HeroImageDto update(@PathVariable UUID id, @Valid @RequestBody UpdateHeroImageRequest request) {
        return heroImageService.update(id, request);
    }

    @PutMapping("/reorder")
    public void reorder(@Valid @RequestBody ReorderHeroImagesRequest request) {
        heroImageService.reorder(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        heroImageService.delete(id);
    }
}
