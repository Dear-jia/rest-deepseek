package com.wenfeng.config;

import com.wenfeng.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 强制管理员首次登录后修改初始密码：未改密码前，除修改密码页外禁止访问后台其他页面。
 */
@Component
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public MustChangePasswordFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String uri = request.getRequestURI();
        boolean isAdminArea = uri.startsWith("/admin");
        boolean isAllowed = uri.equals("/admin/account") || uri.equals("/admin/login") || uri.equals("/admin/logout");
        boolean authenticated = auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        if (isAdminArea && !isAllowed && authenticated) {
            userRepository.findByUsername(auth.getName()).ifPresent(user -> {
                if (user.isMustChangePassword()) {
                    try {
                        response.sendRedirect(request.getContextPath() + "/admin/account?forced=1");
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
        }
        if (!response.isCommitted()) {
            chain.doFilter(request, response);
        }
    }
}
