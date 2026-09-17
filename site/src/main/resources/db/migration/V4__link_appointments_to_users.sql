ALTER TABLE appointments ADD COLUMN user_id UUID;
CREATE INDEX idx_appointments_user_id ON appointments (user_id);
