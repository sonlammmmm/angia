package vn.dichvuangia.management.security;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service // Service xử lý logic rate limit
public class RateLimitService {

    private final RateLimitProperties properties; // Cấu hình rate limit
    private final Clock clock; // Clock để kiểm soát thời gian (testable)
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>(); // Lưu bucket theo key

    public RateLimitService(RateLimitProperties properties) { // Constructor mặc định
        this(properties, Clock.systemUTC()); // Dùng UTC clock
    }

    RateLimitService(RateLimitProperties properties, Clock clock) { // Constructor cho test
        this.properties = properties; // Gán cấu hình
        this.clock = clock; // Gán clock
    }

    public RateLimitResult checkAndIncrement(String key) { // Kiểm tra và tăng bộ đếm
        if (!properties.isEnabled()) { // Nếu tắt rate limit thì luôn cho phép
            long resetAt = Instant.now(clock).getEpochSecond() + properties.getWindowSeconds(); // Tính mốc reset giả
            return new RateLimitResult(true, properties.getMaxRequests(), 0, resetAt); // Trả về cho phép
        }

        synchronized (this) { // Đồng bộ để tránh race condition
            long nowMillis = Instant.now(clock).toEpochMilli(); // Thời gian hiện tại (ms)
            long windowMillis = properties.getWindowSeconds() * 1000L; // Độ dài cửa sổ (ms)
            RateLimitBucket bucket = buckets.get(key); // Lấy bucket theo key

            if (bucket == null || nowMillis - bucket.windowStartMillis >= windowMillis) { // Nếu chưa có hoặc hết cửa sổ
                bucket = new RateLimitBucket(nowMillis, 0); // Tạo bucket mới
                buckets.put(key, bucket); // Lưu bucket
            }

            if (bucket.count >= properties.getMaxRequests()) { // Nếu vượt giới hạn
                long resetAtEpochSeconds = (bucket.windowStartMillis + windowMillis) / 1000L; // Thời điểm reset
                long retryAfter = Math.max(0, resetAtEpochSeconds - Instant.ofEpochMilli(nowMillis).getEpochSecond()); // Số giây cần chờ
                return new RateLimitResult(false, 0, retryAfter, resetAtEpochSeconds); // Trả về bị chặn
            }

            bucket.count += 1; // Tăng bộ đếm
            int remaining = Math.max(0, properties.getMaxRequests() - bucket.count); // Tính còn lại
            long resetAtEpochSeconds = (bucket.windowStartMillis + windowMillis) / 1000L; // Mốc reset
            return new RateLimitResult(true, remaining, 0, resetAtEpochSeconds); // Trả về cho phép
        }
    }

    private static final class RateLimitBucket {
        private final long windowStartMillis; // Thời điểm bắt đầu cửa sổ
        private int count; // Số request đã dùng

        private RateLimitBucket(long windowStartMillis, int count) { // Constructor bucket
            this.windowStartMillis = windowStartMillis; // Gán thời điểm bắt đầu
            this.count = count; // Gán số request
        }
    }
}
