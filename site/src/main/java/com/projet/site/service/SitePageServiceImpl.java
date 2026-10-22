package com.projet.site.service;

import com.projet.site.domain.SitePage;
import com.projet.site.dto.SitePageDto;
import com.projet.site.dto.SitePageSummaryDto;
import com.projet.site.exception.InvalidSitePageException;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.repository.SitePageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class SitePageServiceImpl implements SitePageService {

    static final int MAX_CONTENT_CHARS = 200_000;
    private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private final SitePageRepository sitePageRepository;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional(readOnly = true)
    public SitePageDto get(String slug) {
        requireValidSlug(slug);
        SitePage page = sitePageRepository.findById(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Page introuvable : " + slug));
        return toDto(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SitePageSummaryDto> findAll() {
        return sitePageRepository.findAllByOrderBySlugAsc().stream()
                .map(page -> new SitePageSummaryDto(page.getSlug(), page.getUpdatedAt()))
                .toList();
    }

    @Override
    public SitePageDto save(String slug, JsonNode content) {
        requireValidSlug(slug);
        if (content == null || !content.isObject()) {
            throw new InvalidSitePageException("Le contenu d'une page doit être un objet JSON.");
        }
        String json = content.toString();
        if (json.length() > MAX_CONTENT_CHARS) {
            throw new InvalidSitePageException("Le contenu de la page est trop volumineux.");
        }
        SitePage page = sitePageRepository.findById(slug).orElseGet(() -> new SitePage(slug, json, Instant.now()));
        page.setContent(json);
        page.setUpdatedAt(Instant.now());
        return toDto(sitePageRepository.save(page));
    }

    @Override
    public void reset(String slug) {
        requireValidSlug(slug);
        sitePageRepository.deleteById(slug);
    }

    private static void requireValidSlug(String slug) {
        if (slug == null || slug.length() > 80 || !SLUG.matcher(slug).matches()) {
            throw new InvalidSitePageException("Identifiant de page invalide.");
        }
    }

    private SitePageDto toDto(SitePage page) {
        return new SitePageDto(page.getSlug(), jsonMapper.readTree(page.getContent()), page.getUpdatedAt());
    }
}
