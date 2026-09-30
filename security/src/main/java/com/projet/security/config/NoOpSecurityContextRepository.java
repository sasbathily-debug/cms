package com.projet.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * We authenticate purely from the JWT cookie on every request (see JwtAuthenticationFilter) and
 * never load/save a SecurityContext from any session-backed store. SessionManagementFilter uses
 * containsContext() to decide whether the authentication established during this request is "new";
 * the honest answer in a stateless setup is always false, which makes it run every registered
 * SessionAuthenticationStrategy -- including CsrfAuthenticationStrategy, which Spring wires in
 * automatically once CSRF protection is enabled -- on literally every authenticated request (there
 * is no session to remember "already handled"). That rotates the CSRF cookie constantly and, due to
 * how the strategy replaces the request attribute without persisting it, effectively deletes it on
 * every other authenticated call. Answering true here opts a fully stateless app out of that
 * machinery, which exists to protect session-based apps from fixation -- not relevant here.
 */
public class NoOpSecurityContextRepository implements SecurityContextRepository {

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        return SecurityContextHolder.createEmptyContext();
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        // no-op: JwtAuthenticationFilter re-derives the context from the cookie on every request
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return true;
    }
}
