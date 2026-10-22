package com.projet.site.service;

import com.projet.site.domain.SitePage;
import com.projet.site.dto.SitePageDto;
import com.projet.site.exception.InvalidSitePageException;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.repository.SitePageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SitePageServiceTest {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private SitePageRepository repository;
    private SitePageServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(SitePageRepository.class);
        service = new SitePageServiceImpl(repository, mapper);
        when(repository.save(any(SitePage.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private JsonNode json(String text) {
        return mapper.readTree(text);
    }

    @Test
    void enregistreUnObjetEtLeRelitTelQuel() {
        when(repository.findById("a-propos")).thenReturn(Optional.empty());

        SitePageDto saved = service.save("a-propos", json("{\"titre\":\"Dr Aïssatou\",\"liste\":[{\"a\":1}]}"));

        assertThat(saved.slug()).isEqualTo("a-propos");
        assertThat(saved.content().get("titre").asString()).isEqualTo("Dr Aïssatou");
        assertThat(saved.content().get("liste").get(0).get("a").asInt()).isEqualTo(1);
        ArgumentCaptor<SitePage> page = ArgumentCaptor.forClass(SitePage.class);
        verify(repository).save(page.capture());
        assertThat(page.getValue().getContent()).contains("Dr Aïssatou");
    }

    @Test
    void remplaceLeContenuDUnePageExistante() {
        SitePage existing = new SitePage("le-cabinet", "{\"v\":1}", Instant.parse("2026-01-01T00:00:00Z"));
        when(repository.findById("le-cabinet")).thenReturn(Optional.of(existing));

        service.save("le-cabinet", json("{\"v\":2}"));

        assertThat(existing.getContent()).isEqualTo("{\"v\":2}");
        assertThat(existing.getUpdatedAt()).isAfter(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void refuseUnContenuQuiNEstPasUnObjet() {
        for (String text : new String[]{"[1,2]", "\"texte\"", "12", "null"}) {
            assertThatThrownBy(() -> service.save("a-propos", json(text)))
                    .isInstanceOf(InvalidSitePageException.class)
                    .hasMessageContaining("objet JSON");
        }
        verify(repository, never()).save(any());
    }

    @Test
    void refuseUnIdentifiantMalForme() {
        for (String slug : new String[]{"A-Propos", "a propos", "a_propos", "-a", "a-", "a--b", "../etc", ""}) {
            assertThatThrownBy(() -> service.save(slug, json("{}")))
                    .as(slug)
                    .isInstanceOf(InvalidSitePageException.class);
        }
        assertThatThrownBy(() -> service.save("x".repeat(81), json("{}")))
                .isInstanceOf(InvalidSitePageException.class);
    }

    @Test
    void refuseUnContenuTropVolumineux() {
        String big = "{\"texte\":\"" + "a".repeat(SitePageServiceImpl.MAX_CONTENT_CHARS) + "\"}";

        assertThatThrownBy(() -> service.save("a-propos", json(big)))
                .isInstanceOf(InvalidSitePageException.class)
                .hasMessageContaining("volumineux");
    }

    @Test
    void unePageSansContenuSaisiEstIntrouvable() {
        when(repository.findById("contact")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get("contact")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void reinitialiserSupprimeLeContenuSaisi() {
        service.reset("a-propos");

        verify(repository).deleteById("a-propos");
        assertThatThrownBy(() -> service.reset("A Propos")).isInstanceOf(InvalidSitePageException.class);
    }
}
