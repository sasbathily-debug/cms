package com.projet.site.mapper;

import com.projet.site.domain.FaqItem;
import com.projet.site.dto.FaqItemDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FaqItemMapper {

    FaqItemDto toDto(FaqItem faqItem);
}
