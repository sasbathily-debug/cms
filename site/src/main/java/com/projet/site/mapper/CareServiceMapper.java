package com.projet.site.mapper;

import com.projet.site.domain.CareService;
import com.projet.site.dto.CareServiceDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CareServiceMapper {

    CareServiceDto toDto(CareService careService);
}
