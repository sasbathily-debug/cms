package com.projet.site.mapper;

import com.projet.site.domain.ScheduleException;
import com.projet.site.dto.ScheduleExceptionDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScheduleExceptionMapper {

    ScheduleExceptionDto toDto(ScheduleException exception);
}
