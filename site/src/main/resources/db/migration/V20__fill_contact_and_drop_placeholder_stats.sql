-- Coordonnées du cabinet : renseignées seulement si le champ est encore vide,
-- pour ne jamais écraser une valeur saisie dans l'admin.
UPDATE site_settings SET phone = '221772263906' WHERE id = 1 AND phone IS NULL;
UPDATE site_settings SET whatsapp_number = '221772263906' WHERE id = 1 AND whatsapp_number IS NULL;
UPDATE site_settings SET email = 'contact@draissatoumamadou.com' WHERE id = 1 AND email IS NULL;

-- Les chiffres par défaut de V2 étaient des valeurs de remplissage, pas des données
-- réelles. On ne les retire que s'ils sont encore exactement ces valeurs ; le site
-- n'affiche une statistique que si elle est renseignée.
UPDATE site_settings SET years_experience = NULL WHERE id = 1 AND years_experience = '15+';
UPDATE site_settings SET patients_count = NULL WHERE id = 1 AND patients_count = '2 400+';
UPDATE site_settings SET satisfaction_rating = NULL WHERE id = 1 AND satisfaction_rating = '4.9/5';
