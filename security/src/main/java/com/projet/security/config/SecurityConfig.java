package com.projet.security.config;

import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.projet.security.jwt.CsrfCookieFilter;
import com.projet.security.jwt.JwtAuthenticationFilter;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, OtpProperties.class, CookieProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        // logout CSRF is low-severity (forces a log-out at worst, no data/privilege impact) and
                        // exempting it avoids spurious 401s from token rotation racing a still-valid session cookie
                        .ignoringRequestMatchers("/auth/logout"))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/me", "/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users", "/users/*").hasRole("ADMIN")
                        .requestMatchers("/auth/**", "/actuator/health/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                        .requestMatchers(HttpMethod.POST, "/appointments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/site/services", "/site/faq", "/site/settings", "/site/health-tips", "/site/availability", "/site/hero-images").permitAll()
                        .requestMatchers(HttpMethod.GET, "/appointments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/appointments/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/site/services", "/site/faq", "/site/health-tips", "/site/hero-images").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/site/services/*", "/site/faq/*", "/site/settings", "/site/health-tips/*", "/site/hero-images/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/site/services/*", "/site/faq/*", "/site/health-tips/*", "/site/hero-images/*").hasRole("ADMIN")
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
        configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

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
