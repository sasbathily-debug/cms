ALTER TABLE users
    ADD COLUMN phone VARCHAR(30),
    ADD COLUMN date_of_birth DATE,
    ADD COLUMN email_reminders BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN sms_reminders BOOLEAN NOT NULL DEFAULT false;
