package com.projet.site.controller;

import com.projet.site.domain.AppointmentStatus;
import com.projet.site.dto.AppointmentDto;
import com.projet.site.dto.AppointmentRequest;
import com.projet.site.dto.AppointmentStatusEventDto;
import com.projet.site.dto.UpdateAppointmentStatusRequest;
import com.projet.site.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDto create(@Valid @RequestBody AppointmentRequest request, Authentication authentication) {
        return appointmentService.create(request, currentUserIdOrNull(authentication));
    }

    @GetMapping("/me")
    public Page<AppointmentDto> findMine(Authentication authentication, Pageable pageable) {
        return appointmentService.findAllForUser(UUID.fromString(authentication.getName()), pageable);
    }

    @GetMapping
    public Page<AppointmentDto> findAll(@RequestParam(required = false) AppointmentStatus status, Pageable pageable) {
        return appointmentService.findAll(status, pageable);
    }

    @PatchMapping("/{id}/status")
    public AppointmentDto updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        return appointmentService.updateStatus(id, request.status());
    }

    @GetMapping("/{id}/history")
    public List<AppointmentStatusEventDto> history(@PathVariable UUID id, Authentication authentication) {
        return appointmentService.findHistory(id, currentUserIdOrNull(authentication), isAdmin(authentication));
    }

    private UUID currentUserIdOrNull(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return UUID.fromString(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
