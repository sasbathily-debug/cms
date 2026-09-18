package com.projet.site.mapper;

import com.projet.site.domain.HealthTip;
import com.projet.site.dto.HealthTipDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthTipMapper {

    HealthTipDto toDto(HealthTip healthTip);
}
