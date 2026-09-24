-- Tracks whether the 24h-before reminder has already been sent for an
-- appointment, so the daily scheduled job never sends it twice.
ALTER TABLE appointments ADD COLUMN reminder_sent BOOLEAN NOT NULL DEFAULT false;
