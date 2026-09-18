CREATE TABLE health_tips (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    icon VARCHAR(100),
    display_order INT NOT NULL DEFAULT 0
);

INSERT INTO health_tips (id, title, body, icon, display_order) VALUES
    (gen_random_uuid(), 'Surveillez votre glycémie régulièrement', 'Un contrôle régulier de la glycémie permet d''ajuster le traitement à temps et d''éviter les complications à long terme.', 'droplet', 1),
    (gen_random_uuid(), 'Privilégiez une alimentation équilibrée', 'Limitez les sucres rapides et les graisses saturées ; privilégiez les fibres, les légumes et les protéines maigres à chaque repas.', 'leaf', 2),
    (gen_random_uuid(), 'Bougez au moins 30 minutes par jour', 'Une activité physique régulière améliore la sensibilité à l''insuline et aide à maintenir un poids stable.', 'activity', 3);
