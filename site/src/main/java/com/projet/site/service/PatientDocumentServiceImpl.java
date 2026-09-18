package com.projet.site.service;

import com.projet.site.domain.PatientDocument;
import com.projet.site.dto.PatientDocumentDto;
import com.projet.site.dto.PatientDocumentRequest;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.mapper.PatientDocumentMapper;
import com.projet.site.repository.PatientDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientDocumentServiceImpl implements PatientDocumentService {

    private final PatientDocumentRepository patientDocumentRepository;
    private final PatientDocumentMapper patientDocumentMapper;

    @Override
    public PatientDocumentDto create(PatientDocumentRequest request) {
        PatientDocument document = new PatientDocument(
                request.userId(),
                request.appointmentId(),
                request.fileName(),
                request.fileUrl(),
                request.category()
        );
        return patientDocumentMapper.toDto(patientDocumentRepository.save(document));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDocumentDto> findForUser(UUID userId, Pageable pageable) {
        return patientDocumentRepository.findByUserIdOrderByUploadedAtDesc(userId, pageable)
                .map(patientDocumentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDocumentDto> findAll(UUID userId, Pageable pageable) {
        Page<PatientDocument> page = userId == null
                ? patientDocumentRepository.findAllByOrderByUploadedAtDesc(pageable)
                : patientDocumentRepository.findByUserIdOrderByUploadedAtDesc(userId, pageable);
        return page.map(patientDocumentMapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        if (!patientDocumentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Document introuvable : " + id);
        }
        patientDocumentRepository.deleteById(id);
    }
}
