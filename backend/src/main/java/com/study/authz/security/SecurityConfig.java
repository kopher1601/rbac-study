package com.study.authz.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 7 설정.
 *
 * <p>학습용 단순화 결정:
 * <ul>
 *   <li><b>formLogin</b>(시드 유저 + 공통 비밀번호). 성공/실패를 리다이렉트 대신 JSON 200/401 로 응답해 SPA 친화.
 *       성공 시 폼 인증 필터가 SecurityContext 를 세션에 자동 저장(JSESSIONID)하므로 수동 저장 불필요.
 *   <li><b>CSRF disable</b>: 학습용 API + Vite same-origin 프록시. (운영이라면 세션 쿠키 기반이므로 CSRF 필요.)
 *   <li><b>역할 계층</b>은 {@link RoleHierarchy} 빈으로 선언하고, 메서드 보안에 연결하기 위해
 *       {@link MethodSecurityExpressionHandler} 를 {@code static @Bean} 으로 노출한다.
 * </ul>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                // h2-console 는 iframe 으로 렌더링되므로 같은 출처 프레이밍 허용
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login", "/api/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
                        .requestMatchers("/api/me").authenticated()
                        .requestMatchers("/api/rbac/**").authenticated()
                        .anyRequest().denyAll())
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .successHandler(jsonAuthenticationSuccessHandler())
                        .failureHandler(jsonAuthenticationFailureHandler()))
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(jsonLogoutSuccessHandler()))
                // 미인증 요청은 로그인 페이지 리다이렉트 대신 401 (SPA/API)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    /**
     * VIEWER &lt; EDITOR &lt; ADMIN. {@code hasRole('VIEWER')} 를 상위 역할도 통과시킨다.
     * Security 7 에서는 RoleHierarchy 빈을 선언하면 authorizeHttpRequests 와 메서드 보안(@PreAuthorize)이
     * 모두 자동으로 picks up 하므로, 별도의 MethodSecurityExpressionHandler 빈은 필요 없다.
     */
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("EDITOR")
                .role("EDITOR").implies("VIEWER")
                .build();
    }

    /** Vite dev 서버(5173) 직접 호출에 대한 안전망. 평소엔 dev 프록시로 same-origin 이라 불필요. */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private AuthenticationSuccessHandler jsonAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter()
                    .write("{\"username\":\"" + authentication.getName() + "\",\"authenticated\":true}");
        };
    }

    private AuthenticationFailureHandler jsonAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"invalid_credentials\"}");
        };
    }

    private LogoutSuccessHandler jsonLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"loggedOut\":true}");
        };
    }
}
