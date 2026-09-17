package com.projet.user.dto;

import com.projet.user.domain.Role;

import java.util.UUID;

public record UserDto(UUID id, String email, String firstName, String lastName, Role role) {
}
