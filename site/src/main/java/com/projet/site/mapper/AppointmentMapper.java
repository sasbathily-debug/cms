package com.projet.site.mapper;

import com.projet.site.domain.Appointment;
import com.projet.site.dto.AppointmentDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    AppointmentDto toDto(Appointment appointment);
}
