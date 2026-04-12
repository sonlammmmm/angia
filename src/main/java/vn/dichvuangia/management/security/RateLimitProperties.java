package vn.dichvuangia.management.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component // Đăng ký bean cấu hình cho rate limit
@ConfigurationProperties(prefix = "app.rate-limit") // Prefix cấu hình trong application.properties
public class RateLimitProperties {

    private boolean enabled = false; // Bật/tắt rate limit
    private int maxRequests = 100; // Số request tối đa trong 1 cửa sổ
    private long windowSeconds = 3600; // Thời gian cửa sổ (giây)

    public boolean isEnabled() { // Getter bật/tắt
        return enabled;
    }

    public void setEnabled(boolean enabled) { // Setter bật/tắt
        this.enabled = enabled;
    }

    public int getMaxRequests() { // Getter số request tối đa
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) { // Setter số request tối đa
        this.maxRequests = maxRequests;
    }

    public long getWindowSeconds() { // Getter thời gian cửa sổ
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) { // Setter thời gian cửa sổ
        this.windowSeconds = windowSeconds;
    }
}
