package com.projet.site.mapper;

import com.projet.site.domain.HeroImage;
import com.projet.site.dto.HeroImageDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HeroImageMapper {

    HeroImageDto toDto(HeroImage heroImage);
}
