package com.wenfeng.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class RateLimitedAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final LoginAttemptService attempts;

    public RateLimitedAuthenticationFailureHandler(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String key = attempts.key(request.getParameter("username"), request);
        attempts.registerFailure(key);
        boolean blocked = attempts.isBlocked(key);
        String base = request.getRequestURI().contains("/user") ? "/user/login" : "/admin/login";
        response.sendRedirect(request.getContextPath() + base + (blocked ? "?locked" : "?error"));
    }
}
