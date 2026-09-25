-- A slot is now mandatory when booking, so every appointment is auto-confirmed
-- on creation and the PENDING status is no longer reachable. Migrate any
-- leftover rows before the Java enum value disappears.
UPDATE appointments SET status = 'CONFIRMED' WHERE status = 'PENDING';

ALTER TABLE appointments ALTER COLUMN status SET DEFAULT 'CONFIRMED';
