package com.projet.site.mapper;

import com.projet.site.domain.AvailabilityRule;
import com.projet.site.dto.AvailabilityRuleDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AvailabilityRuleMapper {

    AvailabilityRuleDto toDto(AvailabilityRule rule);
}
