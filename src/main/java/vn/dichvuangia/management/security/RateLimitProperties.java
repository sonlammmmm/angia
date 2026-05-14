package vn.dichvuangia.management.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = false;
    private int maxRequests = 100;
    private long windowSeconds = 3600;
    private int loginMaxRequests = 5;
    private long loginWindowSeconds = 60;
    private int registerMaxRequests = 3;
    private long registerWindowSeconds = 60;
    private int refreshMaxRequests = 10;
    private long refreshWindowSeconds = 60;
    private boolean trustForwardedFor = false;
    private int maxBuckets = 100000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public int getLoginMaxRequests() {
        return loginMaxRequests;
    }

    public void setLoginMaxRequests(int loginMaxRequests) {
        this.loginMaxRequests = loginMaxRequests;
    }

    public long getLoginWindowSeconds() {
        return loginWindowSeconds;
    }

    public void setLoginWindowSeconds(long loginWindowSeconds) {
        this.loginWindowSeconds = loginWindowSeconds;
    }

    public int getRegisterMaxRequests() {
        return registerMaxRequests;
    }

    public void setRegisterMaxRequests(int registerMaxRequests) {
        this.registerMaxRequests = registerMaxRequests;
    }

    public long getRegisterWindowSeconds() {
        return registerWindowSeconds;
    }

    public void setRegisterWindowSeconds(long registerWindowSeconds) {
        this.registerWindowSeconds = registerWindowSeconds;
    }

    public int getRefreshMaxRequests() {
        return refreshMaxRequests;
    }

    public void setRefreshMaxRequests(int refreshMaxRequests) {
        this.refreshMaxRequests = refreshMaxRequests;
    }

    public long getRefreshWindowSeconds() {
        return refreshWindowSeconds;
    }

    public void setRefreshWindowSeconds(long refreshWindowSeconds) {
        this.refreshWindowSeconds = refreshWindowSeconds;
    }

    public boolean isTrustForwardedFor() {
        return trustForwardedFor;
    }

    public void setTrustForwardedFor(boolean trustForwardedFor) {
        this.trustForwardedFor = trustForwardedFor;
    }

    public int getMaxBuckets() {
        return maxBuckets;
    }

    public void setMaxBuckets(int maxBuckets) {
        this.maxBuckets = maxBuckets;
    }
}
