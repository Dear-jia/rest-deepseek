package com.wenfeng.config;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * 简单的登录防爆破：同一用户名+IP 连续失败 5 次后锁定 15 分钟。
 * 单实例内存实现（Render 免费版单实例，够用；多实例部署时可换 Redis）。
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private record Attempt(int failures, Instant lockedUntil) {
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return false;
        }
        if (attempt.lockedUntil() != null && attempt.lockedUntil().isAfter(Instant.now())) {
            return true;
        }
        if (attempt.lockedUntil() != null) {
            attempts.remove(key);
        }
        return false;
    }

    public void registerFailure(String key) {
        attempts.compute(key, (k, old) -> {
            int failures = (old == null ? 0 : old.failures()) + 1;
            Instant lock = failures >= MAX_ATTEMPTS ? Instant.now().plus(LOCK_DURATION) : null;
            return new Attempt(failures, lock);
        });
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    public String key(String username, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            ip = forwarded.split(",")[0].trim();
        }
        return (username == null ? "" : username) + "|" + ip;
    }
}
