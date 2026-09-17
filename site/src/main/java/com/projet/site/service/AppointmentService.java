package com.projet.site.service;

import com.projet.site.domain.AppointmentStatus;
import com.projet.site.dto.AppointmentDto;
import com.projet.site.dto.AppointmentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AppointmentService {

    AppointmentDto create(AppointmentRequest request);

    Page<AppointmentDto> findAll(AppointmentStatus status, Pageable pageable);

    AppointmentDto updateStatus(UUID id, AppointmentStatus status);
}
