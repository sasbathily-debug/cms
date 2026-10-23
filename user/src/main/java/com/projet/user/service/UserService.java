package com.projet.user.service;

import com.projet.user.dto.ChangePasswordRequest;
import com.projet.user.dto.CreateUserCommand;
import com.projet.user.dto.UpdateProfileRequest;
import com.projet.user.dto.UserAuthView;
import com.projet.user.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserDto createUser(CreateUserCommand command);

    Optional<UserDto> findByEmail(String email);

    Optional<UserAuthView> findAuthViewByEmail(String email);

    Optional<UserAuthView> findAuthViewByIdentifier(String identifier);

    UserDto getById(UUID id);

    Page<UserDto> findAll(Pageable pageable);

    UserDto confirmEmail(String email);

    UserDto updateProfile(UUID id, UpdateProfileRequest request);

    void changePassword(UUID id, ChangePasswordRequest request);

    /** Remplace le mot de passe sans demander l'ancien : réservé à la réinitialisation par code reçu par e-mail. */
    void resetPassword(UUID id, String newPasswordHash);
}
