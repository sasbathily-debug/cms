package com.projet.site.dto;

public record SiteSettingsDto(
        String heroTitle,
        String heroSubtitle,
        String aboutText,
        String yearsExperience,
        String patientsCount,
        String satisfactionRating,
        String aboutImageUrl,
        String address,
        String phone,
        String email,
        String whatsappNumber,
        String openingHours
) {
}
