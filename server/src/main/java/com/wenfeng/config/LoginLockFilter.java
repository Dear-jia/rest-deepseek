package com.wenfeng.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 登录锁定检查：在认证之前拦截，已被锁定的账号即使密码正确也禁止登录。
 */
@Component
public class LoginLockFilter extends OncePerRequestFilter {

    private final LoginAttemptService attempts;

    public LoginLockFilter(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        boolean isLoginPost = request.getMethod().equalsIgnoreCase("POST")
                && (uri.equals("/admin/login") || uri.equals("/user/login"));
        if (isLoginPost && attempts.isBlocked(attempts.key(request.getParameter("username"), request))) {
            String base = uri.contains("/user") ? "/user/login" : "/admin/login";
            response.sendRedirect(request.getContextPath() + base + "?locked");
            return;
        }
        chain.doFilter(request, response);
    }
}
