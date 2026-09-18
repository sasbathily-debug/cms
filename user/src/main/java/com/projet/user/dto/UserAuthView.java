package com.projet.user.dto;

import com.projet.user.domain.Role;

import java.util.UUID;

/**
 * Vue restreinte exposée au module security pour l'authentification,
 * afin qu'il n'ait jamais besoin de dépendre du repository ni de l'entité User.
 */
public record UserAuthView(UUID id, String email, String firstName, String passwordHash, Role role, boolean enabled) {
}
