-- Contenu du site piloté depuis l'admin : articles (catégorie, durée de lecture, photo, publication),
-- FAQ par thème avec publication, repère et numéro d'urgence dans les réglages, horaires par jour.

ALTER TABLE health_tips ADD COLUMN category VARCHAR(60);
ALTER TABLE health_tips ADD COLUMN reading_minutes INT;
ALTER TABLE health_tips ADD COLUMN photo_url TEXT;
ALTER TABLE health_tips ADD COLUMN published BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE health_tips ADD CONSTRAINT health_tips_reading_minutes_range
    CHECK (reading_minutes IS NULL OR reading_minutes BETWEEN 1 AND 120);

ALTER TABLE faq_items ADD COLUMN category VARCHAR(60);
ALTER TABLE faq_items ADD COLUMN published BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE site_settings ADD COLUMN address_street VARCHAR(255);
ALTER TABLE site_settings ADD COLUMN landmark VARCHAR(255);
ALTER TABLE site_settings ADD COLUMN emergency_number VARCHAR(40);

-- Un jour par ligne (1 = lundi ... 7 = dimanche). Une plage horaire par jour, ou « fermé ».
CREATE TABLE opening_hours (
    day_of_week SMALLINT PRIMARY KEY,
    opens TIME,
    closes TIME,
    closed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT opening_hours_day_range CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT opening_hours_valid_range CHECK (closed OR (opens IS NOT NULL AND closes IS NOT NULL AND opens < closes))
);

-- Mêmes horaires que le texte déjà présent dans les réglages (V2) ; à confirmer avec la doctoresse.
INSERT INTO opening_hours (day_of_week, opens, closes, closed) VALUES
    (1, '09:00', '17:30', FALSE),
    (2, '09:00', '17:30', FALSE),
    (3, '09:00', '17:30', FALSE),
    (4, '09:00', '17:30', FALSE),
    (5, '09:00', '17:30', FALSE),
    (6, '09:00', '13:00', FALSE),
    (7, NULL, NULL, TRUE);
