package com.projet.user.service;

import com.projet.user.domain.User;
import com.projet.user.dto.ChangePasswordRequest;
import com.projet.user.dto.CreateUserCommand;
import com.projet.user.dto.UpdateProfileRequest;
import com.projet.user.dto.UserAuthView;
import com.projet.user.dto.UserDto;
import com.projet.user.exception.EmailAlreadyExistsException;
import com.projet.user.exception.InvalidCurrentPasswordException;
import com.projet.user.exception.UserNotFoundException;
import com.projet.user.mapper.UserMapper;
import com.projet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(CreateUserCommand command) {
        userRepository.findByEmail(command.email()).ifPresent(existing -> {
            throw new EmailAlreadyExistsException(command.email(), existing.isEnabled());
        });
        User user = new User(command.email(), command.passwordHash(), command.firstName(), command.lastName(), command.role());
        user.setEnabled(command.enabled());
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAuthView> findAuthViewByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(u -> new UserAuthView(u.getId(), u.getEmail(), u.getFirstName(), u.getPasswordHash(), u.getRole(), u.isEnabled()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    public UserDto confirmEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        user.setEnabled(true);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto updateProfile(UUID id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setProfilePictureUrl(request.profilePictureUrl());
        user.setPhone(request.phone());
        user.setDateOfBirth(request.dateOfBirth());
        user.setEmailReminders(request.emailReminders());
        user.setSmsReminders(request.smsReminders());
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void changePassword(UUID id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
