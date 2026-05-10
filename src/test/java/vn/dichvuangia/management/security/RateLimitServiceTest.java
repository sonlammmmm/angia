package vn.dichvuangia.management.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {

    @Test
    @DisplayName("Giới hạn request và reset sau 1 giờ")
    void rateLimit_resetsAfterWindow() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setMaxRequests(2);
        properties.setWindowSeconds(3600);

        MutableClock clock = new MutableClock(Instant.parse("2026-04-13T00:00:00Z"));
        RateLimitService service = new RateLimitService(properties, clock);

        RateLimitResult first = service.checkAndIncrement("ip:127.0.0.1", properties.getMaxRequests(), properties.getWindowSeconds());
        RateLimitResult second = service.checkAndIncrement("ip:127.0.0.1", properties.getMaxRequests(), properties.getWindowSeconds());
        RateLimitResult third = service.checkAndIncrement("ip:127.0.0.1", properties.getMaxRequests(), properties.getWindowSeconds());

        assertThat(first.allowed()).isTrue();
        assertThat(second.allowed()).isTrue();
        assertThat(third.allowed()).isFalse();
        assertThat(third.retryAfterSeconds()).isEqualTo(3600);

        clock.advance(Duration.ofHours(1));
        RateLimitResult afterReset = service.checkAndIncrement("ip:127.0.0.1", properties.getMaxRequests(), properties.getWindowSeconds());

        assertThat(afterReset.allowed()).isTrue();
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        public void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
