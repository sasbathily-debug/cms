package com.projet.site.mapper;

import com.projet.site.domain.AppointmentStatusEvent;
import com.projet.site.dto.AppointmentStatusEventDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppointmentStatusEventMapper {

    AppointmentStatusEventDto toDto(AppointmentStatusEvent event);
}
