package com.projet.security.service;

import com.projet.user.dto.UserAuthView;
import com.projet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String identifier) {
        UserAuthView user = userService.findAuthViewByIdentifier(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur pour : " + identifier));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.email())
                .password(user.passwordHash())
                .disabled(!user.enabled())
                .authorities("ROLE_" + user.role().name())
                .build();
    }
}
