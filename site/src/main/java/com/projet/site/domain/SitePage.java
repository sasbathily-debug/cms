package com.projet.site.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Contenu d'une page du site : un document JSON, stocké tel quel. */
@Entity
@Table(name = "site_pages")
@Getter
@Setter
@NoArgsConstructor
public class SitePage {

    @Id
    private String slug;

    @Column(nullable = false)
    private String content;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SitePage(String slug, String content, Instant updatedAt) {
        this.slug = slug;
        this.content = content;
        this.updatedAt = updatedAt;
    }
}
