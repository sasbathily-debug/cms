package com.projet.security.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.projet.security.jwt.JwtAuthenticationFilter;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, OtpProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/me", "/users/me/**").authenticated()
                        .requestMatchers("/auth/**","/users/**", "/actuator/health/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                        .requestMatchers(HttpMethod.POST, "/appointments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/site/services", "/site/faq", "/site/settings", "/site/health-tips", "/site/availability").permitAll()
                        .requestMatchers(HttpMethod.GET, "/appointments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/appointments/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/site/services", "/site/faq", "/site/health-tips").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/site/services/*", "/site/faq/*", "/site/settings", "/site/health-tips/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/site/services/*", "/site/faq/*", "/site/health-tips/*").hasRole("ADMIN")
                        .requestMatchers("/site/availability/rules/**", "/site/availability/exceptions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/documents").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/documents").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/documents/*").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "https://cms-front.vercel.app",
                "https://*.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
