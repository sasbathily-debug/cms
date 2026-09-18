CREATE TABLE availability_rules (
    id UUID PRIMARY KEY,
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT NOT NULL
);

CREATE TABLE schedule_exceptions (
    id UUID PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    closed BOOLEAN NOT NULL DEFAULT true,
    custom_start_time TIME,
    custom_end_time TIME,
    slot_duration_minutes INT,
    note VARCHAR(255)
);

INSERT INTO availability_rules (id, day_of_week, start_time, end_time, slot_duration_minutes) VALUES
    (gen_random_uuid(), 'MONDAY', '09:00', '12:30', 30),
    (gen_random_uuid(), 'MONDAY', '14:00', '17:30', 30),
    (gen_random_uuid(), 'TUESDAY', '09:00', '12:30', 30),
    (gen_random_uuid(), 'TUESDAY', '14:00', '17:30', 30),
    (gen_random_uuid(), 'WEDNESDAY', '09:00', '12:30', 30),
    (gen_random_uuid(), 'WEDNESDAY', '14:00', '17:30', 30),
    (gen_random_uuid(), 'THURSDAY', '09:00', '12:30', 30),
    (gen_random_uuid(), 'THURSDAY', '14:00', '17:30', 30),
    (gen_random_uuid(), 'FRIDAY', '09:00', '12:30', 30),
    (gen_random_uuid(), 'FRIDAY', '14:00', '17:30', 30),
    (gen_random_uuid(), 'SATURDAY', '09:00', '13:00', 30);
