package com.projet.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String profilePictureUrl
) {
}
