package com.projet.site.controller;

import com.projet.site.dto.SitePageDto;
import com.projet.site.dto.SitePageSummaryDto;
import com.projet.site.dto.UpdateSitePageRequest;
import com.projet.site.service.SitePageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/site/pages")
@RequiredArgsConstructor
public class SitePageController {

    private final SitePageService sitePageService;

    /** Réservé à l'admin : les pages dont le contenu a été saisi. */
    @GetMapping
    public List<SitePageSummaryDto> findAll() {
        return sitePageService.findAll();
    }

    /** Public : le site lit ici le contenu de chaque page. */
    @GetMapping("/{slug}")
    public SitePageDto get(@PathVariable String slug) {
        return sitePageService.get(slug);
    }

    @PutMapping("/{slug}")
    public SitePageDto save(@PathVariable String slug, @Valid @RequestBody UpdateSitePageRequest request) {
        return sitePageService.save(slug, request.content());
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@PathVariable String slug) {
        sitePageService.reset(slug);
    }
}
