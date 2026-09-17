package com.projet.site.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSiteSettingsRequest(
        @NotBlank String heroTitle,
        String heroSubtitle,
        String aboutText,
        String yearsExperience,
        String patientsCount,
        String satisfactionRating,
        String heroImageUrl,
        String aboutImageUrl,
        String address,
        String phone,
        String email,
        String whatsappNumber,
        String openingHours
) {
}
