package vn.dichvuangia.management.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RateLimitService {

    private static final long CLEANUP_INTERVAL_OPS = 1024;

    private final RateLimitProperties properties;
    private final Clock clock;
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong operationCounter = new AtomicLong(0);

    @Autowired
    public RateLimitService(RateLimitProperties properties) {
        this(properties, Clock.systemUTC());
    }

    RateLimitService(RateLimitProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public RateLimitResult checkAndIncrement(String key, int maxRequests, long windowSeconds) {
        if (!properties.isEnabled()) {
            long resetAt = Instant.now(clock).getEpochSecond() + windowSeconds;
            return new RateLimitResult(true, maxRequests, 0, resetAt);
        }

        long nowMillis = Instant.now(clock).toEpochMilli();
        long windowMillis = windowSeconds * 1000L;
        AtomicReference<RateLimitResult> resultRef = new AtomicReference<>();

        buckets.compute(key, (k, bucket) -> {
            RateLimitBucket current = bucket;
            if (current == null || nowMillis - current.windowStartMillis >= windowMillis) {
                current = new RateLimitBucket(nowMillis, windowMillis, 0, nowMillis);
            }

            current.lastSeenMillis = nowMillis;

            if (current.count >= maxRequests) {
                long resetAtEpochSeconds = (current.windowStartMillis + windowMillis) / 1000L;
                long retryAfter = Math.max(0,
                        resetAtEpochSeconds - Instant.ofEpochMilli(nowMillis).getEpochSecond());
                resultRef.set(new RateLimitResult(false, 0, retryAfter, resetAtEpochSeconds));
                return current;
            }

            current.count += 1;
            int remaining = Math.max(0, maxRequests - current.count);
            long resetAtEpochSeconds = (current.windowStartMillis + windowMillis) / 1000L;
            resultRef.set(new RateLimitResult(true, remaining, 0, resetAtEpochSeconds));
            return current;
        });

        maybeCleanup(nowMillis);
        return resultRef.get();
    }

    private void maybeCleanup(long nowMillis) {
        long op = operationCounter.incrementAndGet();
        if (op % CLEANUP_INTERVAL_OPS != 0) {
            return;
        }

        Iterator<Map.Entry<String, RateLimitBucket>> it = buckets.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, RateLimitBucket> entry = it.next();
            RateLimitBucket bucket = entry.getValue();
            if (nowMillis - bucket.lastSeenMillis >= bucket.windowMillis) {
                buckets.remove(entry.getKey(), bucket);
            }
        }

        int maxBuckets = properties.getMaxBuckets();
        if (maxBuckets <= 0 || buckets.size() <= maxBuckets) {
            return;
        }

        int toEvict = buckets.size() - maxBuckets;
        Iterator<Map.Entry<String, RateLimitBucket>> evictIt = buckets.entrySet().iterator();
        while (toEvict > 0 && evictIt.hasNext()) {
            Map.Entry<String, RateLimitBucket> entry = evictIt.next();
            if (buckets.remove(entry.getKey(), entry.getValue())) {
                toEvict--;
            }
        }
    }

    private static final class RateLimitBucket {
        private final long windowStartMillis;
        private final long windowMillis;
        private int count;
        private long lastSeenMillis;

        private RateLimitBucket(long windowStartMillis, long windowMillis, int count, long lastSeenMillis) {
            this.windowStartMillis = windowStartMillis;
            this.windowMillis = windowMillis;
            this.count = count;
            this.lastSeenMillis = lastSeenMillis;
        }
    }
}
