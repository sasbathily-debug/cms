package com.projet.site.service;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Freine les faux rendez-vous pris sans compte : un plafond par adresse IP et par heure, un plafond par numéro de
 * téléphone et par jour, et un plafond global. En mémoire, comme {@link ContactRateLimiter} : suffisant pour une seule
 * instance du serveur.
 */
@Component
public class GuestBookingLimiter {

    static final int PER_IP_PER_HOUR = 8;
    static final int PER_PHONE_PER_DAY = 3;
    static final int GLOBAL_PER_HOUR = 120;

    private final Clock clock;
    private final Map<String, Deque<Long>> perIp = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> perPhone = new ConcurrentHashMap<>();
    private final Deque<Long> global = new ArrayDeque<>();

    public GuestBookingLimiter() {
        this(Clock.systemUTC());
    }

    GuestBookingLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Enregistre une tentative ; renvoie {@code false} si l'un des plafonds est atteint (rien n'est alors enregistré). */
    public synchronized boolean tryAcquire(String ip, String phone) {
        long now = clock.millis();
        long hour = now - Duration.ofHours(1).toMillis();
        long day = now - Duration.ofDays(1).toMillis();
        String ipKey = ip == null ? "?" : ip;
        String phoneKey = phone == null ? "?" : phone.replaceAll("\\D", "");

        Deque<Long> ipHits = perIp.computeIfAbsent(ipKey, k -> new ArrayDeque<>());
        Deque<Long> phoneHits = perPhone.computeIfAbsent(phoneKey, k -> new ArrayDeque<>());
        evict(global, hour);
        evict(ipHits, hour);
        evict(phoneHits, day);
        if (global.size() >= GLOBAL_PER_HOUR || ipHits.size() >= PER_IP_PER_HOUR || phoneHits.size() >= PER_PHONE_PER_DAY) {
            return false;
        }
        global.addLast(now);
        ipHits.addLast(now);
        phoneHits.addLast(now);
        if (perIp.size() > 5000 || perPhone.size() > 5000) {
            perIp.values().removeIf(d -> { evict(d, hour); return d.isEmpty(); });
            perPhone.values().removeIf(d -> { evict(d, day); return d.isEmpty(); });
        }
        return true;
    }

    private static void evict(Deque<Long> hits, long threshold) {
        while (!hits.isEmpty() && hits.peekFirst() < threshold) {
            hits.pollFirst();
        }
    }
}
