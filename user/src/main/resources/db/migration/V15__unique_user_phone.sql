-- Null out duplicate phone numbers, keeping the earliest account for each value,
-- so the new unique constraint below can be applied. Multiple NULLs remain allowed.
UPDATE users u
SET phone = NULL
WHERE u.phone IS NOT NULL
  AND EXISTS (
    SELECT 1 FROM users u2
    WHERE u2.phone = u.phone
      AND u2.created_at < u.created_at
  );

ALTER TABLE users ADD CONSTRAINT uq_users_phone UNIQUE (phone);
