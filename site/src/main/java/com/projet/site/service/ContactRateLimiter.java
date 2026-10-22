package com.projet.site.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limiteur en mémoire à fenêtre glissante : un plafond par adresse IP et un plafond global
 * (qui borne le nombre de mails reçus même si quelqu'un falsifie son IP). Suffisant pour une seule
 * instance ; à remplacer par Redis si le backend passe sur plusieurs instances.
 */
@Component
public class ContactRateLimiter {

    static final int PER_IP_LIMIT = 5;
    static final int GLOBAL_LIMIT = 60;
    static final Duration WINDOW = Duration.ofHours(1);

    private final Clock clock;
    private final Map<String, Deque<Long>> perIp = new ConcurrentHashMap<>();
    private final Deque<Long> global = new ArrayDeque<>();

    public ContactRateLimiter() {
        this(Clock.systemUTC());
    }

    ContactRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Enregistre une tentative ; renvoie {@code false} si l'un des plafonds est atteint. */
    public boolean tryAcquire(String ip) {
        long now = clock.millis();
        long threshold = now - WINDOW.toMillis();

        synchronized (global) {
            evict(global, threshold);
            if (global.size() >= GLOBAL_LIMIT) {
                return false;
            }
            Deque<Long> hits = perIp.computeIfAbsent(ip, key -> new ArrayDeque<>());
            synchronized (hits) {
                evict(hits, threshold);
                if (hits.size() >= PER_IP_LIMIT) {
                    return false;
                }
                hits.addLast(now);
            }
            global.addLast(now);
            if (perIp.size() > 5000) {
                perIp.entrySet().removeIf(e -> {
                    synchronized (e.getValue()) {
                        evict(e.getValue(), threshold);
                        return e.getValue().isEmpty();
                    }
                });
            }
            return true;
        }
    }

    private static void evict(Deque<Long> hits, long threshold) {
        while (!hits.isEmpty() && hits.peekFirst() < threshold) {
            hits.pollFirst();
        }
    }
}
