CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(255),
    reason VARCHAR(150) NOT NULL,
    preferred_date DATE,
    message TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE care_services (
    id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    icon VARCHAR(50),
    display_order INT NOT NULL DEFAULT 0
);

CREATE TABLE faq_items (
    id UUID PRIMARY KEY,
    question VARCHAR(255) NOT NULL,
    answer TEXT NOT NULL,
    display_order INT NOT NULL DEFAULT 0
);

CREATE TABLE site_settings (
    id INT PRIMARY KEY,
    hero_title VARCHAR(255) NOT NULL,
    hero_subtitle TEXT,
    about_text TEXT,
    years_experience VARCHAR(20),
    patients_count VARCHAR(20),
    satisfaction_rating VARCHAR(20),
    hero_image_url TEXT,
    about_image_url TEXT,
    address VARCHAR(255),
    phone VARCHAR(30),
    email VARCHAR(255),
    whatsapp_number VARCHAR(30),
    opening_hours TEXT,
    CONSTRAINT site_settings_singleton CHECK (id = 1)
);

INSERT INTO site_settings (id, hero_title, hero_subtitle, about_text, years_experience, patients_count, satisfaction_rating, address, opening_hours)
VALUES (
    1,
    'Comprendre votre corps, retrouver votre équilibre.',
    'Le Dr Aissatou Fall accompagne à Dakar les patients vivant avec le diabète, les troubles métaboliques et hormonaux.',
    'Après une formation en endocrinologie et nutrition clinique, le Dr Fall a choisi de consacrer son cabinet aux patients vivant avec le diabète, les troubles métaboliques ou hormonaux.',
    '15+',
    '2 400+',
    '4.9/5',
    'Sacré-Cœur, Dakar',
    E'Lundi – Vendredi : 9h00 – 17h30\nSamedi : 9h00 – 13h00\nDimanche : Fermé'
);

INSERT INTO care_services (id, title, description, icon, display_order) VALUES
    ('11111111-1111-1111-1111-111111111101', 'Diabète & suivi glycémique', 'Diagnostic, équilibrage du traitement et éducation à l''auto-surveillance pour un suivi durable du diabète de type 1 et 2.', 'droplet', 1),
    ('11111111-1111-1111-1111-111111111102', 'Maladies métaboliques', 'Prise en charge du surpoids, du cholestérol et du syndrome métabolique avec un plan nutritionnel construit ensemble.', 'gauge', 2),
    ('11111111-1111-1111-1111-111111111103', 'Troubles hormonaux', 'Thyroïde, ménopause, troubles surrénaliens : bilans hormonaux complets et traitements ajustés dans la durée.', 'molecule', 3);

INSERT INTO faq_items (id, question, answer, display_order) VALUES
    ('22222222-2222-2222-2222-222222222201', 'Que dois-je apporter à ma première consultation ?', 'Vos derniers résultats d''analyses (glycémie, bilan hormonal), la liste de vos traitements en cours et vos éventuels comptes-rendus médicaux.', 1),
    ('22222222-2222-2222-2222-222222222202', 'Le cabinet accepte-t-il les mutuelles ?', 'Oui, la plupart des mutuelles locales sont acceptées. Notre secrétariat peut vérifier votre prise en charge avant votre rendez-vous.', 2),
    ('22222222-2222-2222-2222-222222222203', 'Proposez-vous des téléconsultations ?', 'Les téléconsultations sont possibles pour les suivis de routine, après un premier rendez-vous effectué en cabinet.', 3),
    ('22222222-2222-2222-2222-222222222204', 'Comment suivre ma glycémie entre les rendez-vous ?', 'Un carnet de suivi personnalisé vous est remis, et selon votre profil, le Dr Fall peut vous orienter vers un dispositif de mesure en continu.', 4);
