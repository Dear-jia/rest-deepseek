package com.wenfeng.ai;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final int RATE_LIMIT = 10;       // 每 IP 每分钟最多请求数
    private static final long WINDOW_SECONDS = 60;

    private final AiChatService aiChatService;
    /** IP -> 最近请求时间戳列表（简单内存限流，防滥用） */
    private final Map<String, List<Long>> requestLog = new ConcurrentHashMap<>();

    public AiController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("enabled", aiChatService.isConfigured());
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        if (!allow(request)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "请求太频繁啦，请稍等一下下～");
        }
        String message = payload == null ? "" : String.valueOf(payload.getOrDefault("message", "")).trim();
        if (message.isBlank() || message.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "消息内容不能为空且不超过 500 字");
        }
        if (!aiChatService.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI 小助手正在准备中，请稍后再来～");
        }
        try {
            return Map.of("reply", aiChatService.chat(message));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 服务暂时开小差了，请稍后再试");
        }
    }

    private boolean allow(HttpServletRequest request) {
        String ip = clientIp(request);
        long now = Instant.now().getEpochSecond();
        List<Long> timestamps = requestLog.computeIfAbsent(ip, k -> new ArrayList<>());
        synchronized (timestamps) {
            timestamps.removeIf(t -> now - t > WINDOW_SECONDS);
            if (timestamps.size() >= RATE_LIMIT) {
                return false;
            }
            timestamps.add(now);
            return true;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
