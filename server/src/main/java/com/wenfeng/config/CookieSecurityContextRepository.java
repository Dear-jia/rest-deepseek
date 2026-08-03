package com.wenfeng.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * 基于 HMAC 签名 Cookie 的登录态存储。
 * 管理员（ADMIN_AUTH，路径 /admin）与用户（USER_AUTH，路径 /user）各用独立 Cookie，
 * 从而支持同一浏览器同时登录用户端和管理端。
 */
public class CookieSecurityContextRepository implements SecurityContextRepository {

    private static final long MAX_AGE_SECONDS = 7 * 24 * 3600L;

    private final String cookieName;
    private final String cookiePath;
    private final String expectedRole;
    private final String hmacSecret;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CookieSecurityContextRepository(String cookieName, String cookiePath, String expectedRole,
            String hmacSecret, UserDetailsService userDetailsService) {
        this.cookieName = cookieName;
        this.cookiePath = cookiePath;
        this.expectedRole = expectedRole;
        this.hmacSecret = hmacSecret;
        this.userDetailsService = userDetailsService;
    }

    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        return readContext(requestResponseHolder.getRequest());
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = context.getAuthentication();
        boolean hasRole = authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(expectedRole::equals);
        if (!hasRole) {
            deleteCookie(request, response);
            return;
        }
        setCookie(request, response, buildToken(authentication.getName()));
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        String value = cookieValue(request);
        if (value == null) {
            return false;
        }
        Claims claims = parseToken(value);
        return claims != null && claims.expiresAt() > Instant.now().getEpochSecond();
    }

    private SecurityContext readContext(HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String value = cookieValue(request);
        if (value == null) {
            return context;
        }
        Claims claims = parseToken(value);
        if (claims == null || claims.expiresAt() <= Instant.now().getEpochSecond()) {
            return context;
        }
        try {
            UserDetails user = userDetailsService.loadUserByUsername(claims.username());
            boolean hasRole = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(expectedRole::equals);
            if (!hasRole) {
                return context;
            }
            Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                    user, user.getPassword(), user.getAuthorities());
            context.setAuthentication(authentication);
        } catch (Exception ignored) {
            // 用户不存在或角色不符时按未登录处理
        }
        return context;
    }

    private void setCookie(HttpServletRequest request, HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setPath(cookiePath);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge((int) MAX_AGE_SECONDS);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setPath(cookiePath);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String cookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String buildToken(String username) {
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"u\":\"" + username + "\",\"e\":" + (Instant.now().getEpochSecond() + MAX_AGE_SECONDS) + "}")
                        .getBytes(StandardCharsets.UTF_8));
        return payload + "." + hmac(payload);
    }

    private Claims parseToken(String value) {
        int dot = value.lastIndexOf('.');
        if (dot <= 0) {
            return null;
        }
        String payload = value.substring(0, dot);
        String signature = value.substring(dot + 1);
        if (!MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8), hmac(payload).getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        try {
            byte[] json = Base64.getUrlDecoder().decode(payload);
            JsonNode node = objectMapper.readTree(json);
            return new Claims(node.path("u").asText(), node.path("e").asLong());
        } catch (Exception e) {
            return null;
        }
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 初始化失败", e);
        }
    }

    private record Claims(String username, long expiresAt) {
    }
}
