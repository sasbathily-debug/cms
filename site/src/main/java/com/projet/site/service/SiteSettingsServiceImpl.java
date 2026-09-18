package com.projet.site.service;

import com.projet.site.domain.SiteSettings;
import com.projet.site.dto.SiteSettingsDto;
import com.projet.site.dto.UpdateSiteSettingsRequest;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.mapper.SiteSettingsMapper;
import com.projet.site.repository.SiteSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SiteSettingsServiceImpl implements SiteSettingsService {

    private static final int SETTINGS_ID = 1;

    private final SiteSettingsRepository siteSettingsRepository;
    private final SiteSettingsMapper siteSettingsMapper;

    @Override
    @Transactional(readOnly = true)
    public SiteSettingsDto get() {
        return siteSettingsRepository.findById(SETTINGS_ID)
                .map(siteSettingsMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Réglages du site introuvables"));
    }

    @Override
    public SiteSettingsDto update(UpdateSiteSettingsRequest request) {
        SiteSettings settings = siteSettingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Réglages du site introuvables"));
        settings.setHeroTitle(request.heroTitle());
        settings.setHeroSubtitle(request.heroSubtitle());
        settings.setAboutText(request.aboutText());
        settings.setYearsExperience(request.yearsExperience());
        settings.setPatientsCount(request.patientsCount());
        settings.setSatisfactionRating(request.satisfactionRating());
        settings.setHeroImageUrl(request.heroImageUrl());
        settings.setAboutImageUrl(request.aboutImageUrl());
        settings.setAddress(request.address());
        settings.setPhone(request.phone());
        settings.setEmail(request.email());
        settings.setWhatsappNumber(request.whatsappNumber());
        settings.setOpeningHours(request.openingHours());
        return siteSettingsMapper.toDto(siteSettingsRepository.save(settings));
    }
}
