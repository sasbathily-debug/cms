package com.projet.user.dto;

import com.projet.user.domain.Role;

public record CreateUserCommand(
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        Role role,
        boolean enabled
) {
}
