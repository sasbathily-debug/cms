package com.projet.site.repository;

import com.projet.site.domain.PatientDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientDocumentRepository extends JpaRepository<PatientDocument, UUID> {

    Page<PatientDocument> findByUserIdOrderByUploadedAtDesc(UUID userId, Pageable pageable);

    Page<PatientDocument> findAllByOrderByUploadedAtDesc(Pageable pageable);
}
