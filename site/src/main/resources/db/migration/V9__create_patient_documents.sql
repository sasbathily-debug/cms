CREATE TABLE patient_documents (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    appointment_id UUID REFERENCES appointments(id),
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_patient_documents_user_id ON patient_documents(user_id);
