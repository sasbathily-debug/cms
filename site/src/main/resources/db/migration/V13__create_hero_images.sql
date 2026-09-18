CREATE TABLE hero_images (
    id UUID PRIMARY KEY,
    image_url TEXT NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO hero_images (id, image_url, display_order, created_at)
SELECT gen_random_uuid(), hero_image_url, 0, now()
FROM site_settings
WHERE id = 1 AND hero_image_url IS NOT NULL;

ALTER TABLE site_settings DROP COLUMN hero_image_url;
