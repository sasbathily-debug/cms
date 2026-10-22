package com.projet.site.repository;

import com.projet.site.domain.SitePage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SitePageRepository extends JpaRepository<SitePage, String> {

    List<SitePage> findAllByOrderBySlugAsc();
}
