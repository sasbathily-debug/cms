package com.projet.user.controller;

import com.projet.user.dto.ChangePasswordRequest;
import com.projet.user.dto.UpdateProfileRequest;
import com.projet.user.dto.UserDto;
import com.projet.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Page<UserDto> findAll(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/me")
    public UserDto getMe(Authentication authentication) {
        return userService.getById(currentUserId(authentication));
    }

    @PutMapping("/me")
    public UserDto updateMe(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(currentUserId(authentication), request);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUserId(authentication), request);
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
