package com.projet.user.dto;

import com.projet.user.domain.Role;

import java.time.LocalDate;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String profilePictureUrl,
        String phone,
        LocalDate dateOfBirth,
        boolean emailReminders,
        boolean smsReminders,
        Role role
) {
}
