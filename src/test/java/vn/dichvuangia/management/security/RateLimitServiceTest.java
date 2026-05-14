package vn.dichvuangia.management.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    @Test
    @DisplayName("Đồng thời cùng key: chỉ cho phép đúng số lượng maxRequests")
    void rateLimit_concurrentSameKey_isBounded() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setMaxRequests(100);
        properties.setWindowSeconds(3600);

        MutableClock clock = new MutableClock(Instant.parse("2026-04-13T00:00:00Z"));
        RateLimitService service = new RateLimitService(properties, clock);

        ExecutorService pool = Executors.newFixedThreadPool(32);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 500; i++) {
                tasks.add(() -> service
                        .checkAndIncrement("ip:10.0.0.1", properties.getMaxRequests(), properties.getWindowSeconds())
                        .allowed());
            }

            List<Future<Boolean>> futures = pool.invokeAll(tasks);
            long allowedCount = 0;
            for (Future<Boolean> f : futures) {
                if (f.get()) {
                    allowedCount++;
                }
            }

            assertThat(allowedCount).isEqualTo(100);
        } finally {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        }
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
