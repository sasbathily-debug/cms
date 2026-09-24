package com.projet.app.seed;

import com.projet.user.domain.Role;
import com.projet.user.dto.CreateUserCommand;
import com.projet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Jette 3 utilisateurs en base au démarrage pour pouvoir tester l'API sans passer par /auth/register.
 * Désactivé en prod via le profil "prod" (SPRING_PROFILES_ACTIVE=prod sur ECS).
 */
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seed("admin@projet.local", "Admin123!", "Admin", "Système", "221770000001", Role.ADMIN);
        seed("john.doe@projet.local", "Password123!", "John", "Doe", "221770000002", Role.USER);
        seed("jane.doe@projet.local", "Password123!", "Jane", "Doe", "221770000003", Role.USER);
    }

    private void seed(String email, String rawPassword, String firstName, String lastName, String phone, Role role) {
        if (userService.findByEmail(email).isPresent()) {
            return;
        }
        userService.createUser(new CreateUserCommand(
                email, passwordEncoder.encode(rawPassword), firstName, lastName, phone, role, true));
    }
}
