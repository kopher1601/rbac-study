package com.study.authz.auth;

import com.study.authz.auth.dto.MeResponse;
import com.study.authz.auth.dto.UserSummary;
import com.study.authz.domain.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 조회 엔드포인트.
 * 로그인(POST /api/login)·로그아웃(POST /api/logout)은 Spring Security 필터가 처리하므로 여기 없다.
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public AuthController(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    /** 현재 로그인 사용자. 미인증이면 401(SecurityFilter 의 entryPoint 가 먼저 처리하지만 방어적으로 둔다). */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        return currentUserService.currentUser()
                .map(details -> ResponseEntity.ok(MeResponse.from(details)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /** 유저 스위처용 전체 유저 목록(permitAll). */
    @GetMapping("/users")
    @Transactional(readOnly = true)
    public List<UserSummary> users() {
        return userRepository.findAll().stream().map(UserSummary::from).toList();
    }
}
