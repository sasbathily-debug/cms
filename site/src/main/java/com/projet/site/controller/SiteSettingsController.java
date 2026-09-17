package com.projet.site.controller;

import com.projet.site.dto.SiteSettingsDto;
import com.projet.site.dto.UpdateSiteSettingsRequest;
import com.projet.site.service.SiteSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/site/settings")
@RequiredArgsConstructor
public class SiteSettingsController {

    private final SiteSettingsService siteSettingsService;

    @GetMapping
    public SiteSettingsDto get() {
        return siteSettingsService.get();
    }

    @PutMapping
    public SiteSettingsDto update(@Valid @RequestBody UpdateSiteSettingsRequest request) {
        return siteSettingsService.update(request);
    }
}
