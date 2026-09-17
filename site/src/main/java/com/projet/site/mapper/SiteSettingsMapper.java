package com.projet.site.mapper;

import com.projet.site.domain.SiteSettings;
import com.projet.site.dto.SiteSettingsDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SiteSettingsMapper {

    SiteSettingsDto toDto(SiteSettings siteSettings);
}
