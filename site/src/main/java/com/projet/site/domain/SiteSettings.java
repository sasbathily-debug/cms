package com.projet.site.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "site_settings")
@Getter
@Setter
@NoArgsConstructor
public class SiteSettings {

    @Id
    private Integer id = 1;

    @Column(name = "hero_title", nullable = false)
    private String heroTitle;

    @Column(name = "hero_subtitle")
    private String heroSubtitle;

    @Column(name = "about_text")
    private String aboutText;

    @Column(name = "years_experience")
    private String yearsExperience;

    @Column(name = "patients_count")
    private String patientsCount;

    @Column(name = "satisfaction_rating")
    private String satisfactionRating;

    @Column(name = "hero_image_url")
    private String heroImageUrl;

    @Column(name = "about_image_url")
    private String aboutImageUrl;

    private String address;

    private String phone;

    private String email;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Column(name = "opening_hours")
    private String openingHours;
}
