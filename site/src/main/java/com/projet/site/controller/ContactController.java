package com.projet.site.controller;

import com.projet.site.dto.ContactMessageRequest;
import com.projet.site.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/site/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void send(@Valid @RequestBody ContactMessageRequest request, HttpServletRequest http) {
        contactService.send(request, clientIp(http));
    }

    /** Derrière Vercel puis Render, la vraie adresse est le premier élément de X-Forwarded-For. */
    private static String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].strip();
        }
        return http.getRemoteAddr();
    }
}
