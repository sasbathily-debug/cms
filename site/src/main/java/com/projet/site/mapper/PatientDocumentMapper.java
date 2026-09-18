package com.projet.site.mapper;

import com.projet.site.domain.PatientDocument;
import com.projet.site.dto.PatientDocumentDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientDocumentMapper {

    PatientDocumentDto toDto(PatientDocument document);
}
