package com.projet.site.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PatientDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String category;

    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    public PatientDocument(UUID userId, UUID appointmentId, String fileName, String fileUrl, String category) {
        this.userId = userId;
        this.appointmentId = appointmentId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.category = category;
    }
}
