CREATE TABLE appointment_status_events (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL REFERENCES appointments(id),
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_appointment_status_events_appointment_id ON appointment_status_events(appointment_id);

INSERT INTO appointment_status_events (id, appointment_id, previous_status, new_status, created_at)
SELECT gen_random_uuid(), id, NULL, status, created_at
FROM appointments;
