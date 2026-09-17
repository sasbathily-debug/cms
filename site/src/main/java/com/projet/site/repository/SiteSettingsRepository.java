package com.projet.site.repository;

import com.projet.site.domain.SiteSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingsRepository extends JpaRepository<SiteSettings, Integer> {
}
