package com.wenfeng.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    private static final String CSP =
            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data:; connect-src 'self'; font-src 'self'; form-action 'self'; "
            + "frame-ancestors 'self'; base-uri 'self'";

    /** 管理员端 */
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**")
            .securityContext(ctx -> ctx.securityContextRepository(adminRepo))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()
                .anyRequest().hasRole("ADMIN"))
            .formLogin(form -> form
                .loginPage("/admin/login")
                .failureHandler(failureHandler)
                .successHandler((request, response, authentication) ->
                        response.sendRedirect(request.getContextPath() + "/admin")))
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout"))
            .addFilterBefore(mustChangePasswordFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(loginLockFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                .referrerPolicy(referrer -> referrer
                        .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));
        return http.build();
    }

    /** 用户端 */
    @Bean
    @Order(2)
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/user/**")
            .securityContext(ctx -> ctx.securityContextRepository(userRepo))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/login", "/user/register").permitAll()
                .anyRequest().hasRole("USER"))
            .formLogin(form -> form
                .loginPage("/user/login")
                .failureHandler(failureHandler)
                .successHandler((request, response, authentication) ->
                        response.sendRedirect(request.getContextPath() + "/user")))
            .logout(logout -> logout
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/login?logout"))
            .addFilterBefore(loginLockFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                .referrerPolicy(referrer -> referrer
                        .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));
        return http.build();
    }

    /** 公开部分：前台页面、静态资源、公开 API */
    @Bean
    @Order(3)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                .referrerPolicy(referrer -> referrer
                        .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final RateLimitedAuthenticationFailureHandler failureHandler;
    private final MustChangePasswordFilter mustChangePasswordFilter;
    private final LoginLockFilter loginLockFilter;
    private final SecurityContextRepository adminRepo;
    private final SecurityContextRepository userRepo;

    public SecurityConfig(RateLimitedAuthenticationFailureHandler failureHandler,
            MustChangePasswordFilter mustChangePasswordFilter, LoginLockFilter loginLockFilter,
            @Value("${app.auth.secret}") String secret, JpaUserDetailsService userDetailsService) {
        this.failureHandler = failureHandler;
        this.mustChangePasswordFilter = mustChangePasswordFilter;
        this.loginLockFilter = loginLockFilter;
        this.adminRepo = new CookieSecurityContextRepository("ADMIN_AUTH", "/admin", "ROLE_ADMIN", secret, userDetailsService);
        this.userRepo = new CookieSecurityContextRepository("USER_AUTH", "/user", "ROLE_USER", secret, userDetailsService);
    }

    // 两处链注册锁定过滤器（在认证前拦截）
}
