package com.projet.site.controller;

import com.projet.site.dto.CareServiceDto;
import com.projet.site.dto.CareServiceRequest;
import com.projet.site.service.ServiceCatalogService;
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
@RequestMapping("/site/services")
@RequiredArgsConstructor
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    @GetMapping
    public List<CareServiceDto> findAll() {
        return serviceCatalogService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CareServiceDto create(@Valid @RequestBody CareServiceRequest request) {
        return serviceCatalogService.create(request);
    }

    @PutMapping("/{id}")
    public CareServiceDto update(@PathVariable UUID id, @Valid @RequestBody CareServiceRequest request) {
        return serviceCatalogService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        serviceCatalogService.delete(id);
    }
}
