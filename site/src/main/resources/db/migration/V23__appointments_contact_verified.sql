-- Un rendez-vous pris sans compte n'est « vérifié » que si le patient a saisi le code reçu par e-mail.
-- Les rendez-vous existants (pris avec un compte, dont l'adresse est vérifiée) le sont.
ALTER TABLE appointments ADD COLUMN contact_verified BOOLEAN NOT NULL DEFAULT TRUE;
