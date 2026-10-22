package com.projet.site.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ContactRateLimiterTest {

    /** Horloge réglable à la main. */
    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-10-22T10:00:00Z"));

        void advanceMinutes(long minutes) {
            now.updateAndGet(i -> i.plusSeconds(minutes * 60));
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now.get();
        }
    }

    @Test
    void bloqueApresLeNombreMaximumParAdresse() {
        ContactRateLimiter limiter = new ContactRateLimiter(new MutableClock());

        for (int i = 0; i < ContactRateLimiter.PER_IP_LIMIT; i++) {
            assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
        }
        assertThat(limiter.tryAcquire("1.1.1.1")).isFalse();
        assertThat(limiter.tryAcquire("2.2.2.2")).isTrue();
    }

    @Test
    void relacheUneFoisLaFenetrePassee() {
        MutableClock clock = new MutableClock();
        ContactRateLimiter limiter = new ContactRateLimiter(clock);

        for (int i = 0; i < ContactRateLimiter.PER_IP_LIMIT; i++) {
            limiter.tryAcquire("1.1.1.1");
        }
        assertThat(limiter.tryAcquire("1.1.1.1")).isFalse();

        clock.advanceMinutes(61);
        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
    }

    @Test
    void plafonneAussiLeTotalToutesAdressesConfondues() {
        ContactRateLimiter limiter = new ContactRateLimiter(new MutableClock());

        for (int i = 0; i < ContactRateLimiter.GLOBAL_LIMIT; i++) {
            assertThat(limiter.tryAcquire("10.0.0." + i)).isTrue();
        }
        assertThat(limiter.tryAcquire("10.9.9.9")).isFalse();
    }
}
