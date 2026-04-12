package vn.dichvuangia.management.security;

public record RateLimitResult(
        boolean allowed, // true nếu request được phép
        int remaining, // số request còn lại trong cửa sổ
        long retryAfterSeconds, // thời gian cần chờ (giây) nếu bị chặn
        long resetAtEpochSeconds // thời điểm reset cửa sổ (epoch seconds)
) {
}
