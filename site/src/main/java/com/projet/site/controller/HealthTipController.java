package com.projet.site.controller;

import com.projet.site.dto.HealthTipDto;
import com.projet.site.dto.HealthTipRequest;
import com.projet.site.service.HealthTipService;
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
@RequestMapping("/site/health-tips")
@RequiredArgsConstructor
public class HealthTipController {

    private final HealthTipService healthTipService;

    @GetMapping
    public List<HealthTipDto> findAll() {
        return healthTipService.findAll();
    }

    @GetMapping("/all")
    public List<HealthTipDto> findAllForAdmin() {
        return healthTipService.findAllForAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HealthTipDto create(@Valid @RequestBody HealthTipRequest request) {
        return healthTipService.create(request);
    }

    @PutMapping("/{id}")
    public HealthTipDto update(@PathVariable UUID id, @Valid @RequestBody HealthTipRequest request) {
        return healthTipService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        healthTipService.delete(id);
    }
}
