-- Contenu des pages du site (textes, listes, photos) saisi depuis l'admin.
-- Un document JSON par page ; le site garde ses textes d'origine pour tout champ absent.
CREATE TABLE site_pages (
    slug VARCHAR(80) PRIMARY KEY,
    content TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT site_pages_slug_format CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);
