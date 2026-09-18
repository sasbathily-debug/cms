package com.projet.site.repository;

import com.projet.site.domain.HeroImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HeroImageRepository extends JpaRepository<HeroImage, UUID> {

    List<HeroImage> findAllByOrderByDisplayOrderAsc();
}
