package com.projet.site.service;

import com.projet.site.dto.SitePageDto;
import com.projet.site.dto.SitePageSummaryDto;
import tools.jackson.databind.JsonNode;

import java.util.List;

public interface SitePageService {

    /** Le contenu enregistré d'une page. {@code ResourceNotFoundException} si l'admin n'y a encore rien saisi. */
    SitePageDto get(String slug);

    /** Les pages modifiées depuis l'admin. */
    List<SitePageSummaryDto> findAll();

    /** Remplace le contenu d'une page. Refuse un identifiant mal formé, un contenu qui n'est pas un objet JSON ou trop volumineux. */
    SitePageDto save(String slug, JsonNode content);

    /** Supprime le contenu saisi : la page retrouve ses textes d'origine. */
    void reset(String slug);
}
