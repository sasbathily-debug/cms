-- Resolve any pre-existing double-booked confirmed slots by cancelling all
-- but the earliest one, so the new unique constraint below can be applied.
UPDATE appointments a
SET status = 'CANCELLED'
WHERE a.status = 'CONFIRMED'
  AND a.preferred_date IS NOT NULL
  AND a.preferred_time IS NOT NULL
  AND EXISTS (
    SELECT 1 FROM appointments b
    WHERE b.status = 'CONFIRMED'
      AND b.preferred_date = a.preferred_date
      AND b.preferred_time = a.preferred_time
      AND b.created_at < a.created_at
  );

-- Only one confirmed appointment may occupy a given date/time slot.
CREATE UNIQUE INDEX idx_unique_confirmed_appointment_slot
ON appointments (preferred_date, preferred_time)
WHERE status = 'CONFIRMED';
