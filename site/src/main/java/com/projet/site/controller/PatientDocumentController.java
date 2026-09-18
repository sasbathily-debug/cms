package com.projet.site.controller;

import com.projet.site.dto.PatientDocumentDto;
import com.projet.site.dto.PatientDocumentRequest;
import com.projet.site.service.PatientDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class PatientDocumentController {

    private final PatientDocumentService patientDocumentService;

    @GetMapping("/me")
    public Page<PatientDocumentDto> findMine(Authentication authentication, Pageable pageable) {
        return patientDocumentService.findForUser(UUID.fromString(authentication.getName()), pageable);
    }

    @GetMapping
    public Page<PatientDocumentDto> findAll(@RequestParam(required = false) UUID userId, Pageable pageable) {
        return patientDocumentService.findAll(userId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDocumentDto create(@Valid @RequestBody PatientDocumentRequest request) {
        return patientDocumentService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        patientDocumentService.delete(id);
    }
}
