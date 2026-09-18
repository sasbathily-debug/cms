package com.projet.site.service;

import com.projet.site.dto.PatientDocumentDto;
import com.projet.site.dto.PatientDocumentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PatientDocumentService {

    PatientDocumentDto create(PatientDocumentRequest request);

    Page<PatientDocumentDto> findForUser(UUID userId, Pageable pageable);

    Page<PatientDocumentDto> findAll(UUID userId, Pageable pageable);

    void delete(UUID id);
}
