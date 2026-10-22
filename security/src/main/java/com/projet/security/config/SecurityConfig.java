package com.projet.security.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.cors.allowed-origins}")
    private List<String> corsAllowedOrigins;

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
                // SessionManagementFilter runs CsrfAuthenticationStrategy (auto-wired whenever CSRF
                // protection is enabled) any time it thinks "new" authentication just happened, which it
                // decides via securityContextRepository.containsContext(request). Answering false (the
                // default, honest answer for a stateless app) makes every JWT-authenticated request look
                // "new" -- there's no session to remember "already handled" -- so it rotated the CSRF
                // cookie on literally every authenticated call. A NoOpSecurityContextRepository that
                // always answers true opts this fully stateless app out of that session-fixation
                // machinery, which protects session-based apps and has no purpose here.
                .securityContext(context -> context.securityContextRepository(new NoOpSecurityContextRepository()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // Spring Boot forwards here internally on ANY unhandled exception (incl. on an
                        // otherwise-authenticated request), and the security filter chain re-evaluates
                        // on that forward. Without this, a genuine 500 on a protected endpoint gets
                        // reported to the client as a misleading 401 instead of its real status.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/users/me", "/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users", "/users/*").hasRole("ADMIN")
                        .requestMatchers("/auth/**", "/actuator/health/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                        .requestMatchers(HttpMethod.POST, "/appointments").permitAll()
                        .requestMatchers(HttpMethod.POST, "/site/contact").permitAll()
                        .requestMatchers(HttpMethod.GET, "/site/services", "/site/faq", "/site/settings", "/site/health-tips", "/site/opening-hours", "/site/availability", "/site/hero-images").permitAll()
                        .requestMatchers(HttpMethod.GET, "/site/faq/all", "/site/health-tips/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/appointments", "/appointments/counts", "/appointments/stats", "/appointments/export").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/appointments/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/site/services", "/site/faq", "/site/health-tips", "/site/hero-images").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/site/services/*", "/site/faq/*", "/site/settings", "/site/opening-hours", "/site/health-tips/*", "/site/hero-images/*").hasRole("ADMIN")
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
        configuration.setAllowedOrigins(corsAllowedOrigins);
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
