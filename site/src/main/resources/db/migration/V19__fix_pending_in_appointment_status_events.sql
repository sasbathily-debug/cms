-- V18 migrated appointments.status away from PENDING but missed
-- appointment_status_events, which stores the same enum in previous_status
-- and new_status. Every appointment created before V18 still has an initial
-- "Created" history row with new_status = 'PENDING', which Hibernate can no
-- longer deserialize now that the Java enum constant is gone -- this was
-- breaking GET /appointments/{id}/history for every appointment created
-- before that migration.
UPDATE appointment_status_events SET new_status = 'CONFIRMED' WHERE new_status = 'PENDING';
UPDATE appointment_status_events SET previous_status = 'CONFIRMED' WHERE previous_status = 'PENDING';
