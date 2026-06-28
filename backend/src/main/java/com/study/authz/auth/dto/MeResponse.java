package com.study.authz.auth.dto;

import com.study.authz.security.AppUserDetails;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;

/**
 * 현재 로그인 사용자 정보. authorities 에 {@code ROLE_*} 와 권한 문자열을 모두 담아
 * 프런트가 액션 가능 여부를 사전 계산할 수 있게 한다.
 */
public record MeResponse(
        String username,
        String department,
        int clearanceLevel,
        List<String> roles,
        List<String> authorities) {

    public static MeResponse from(AppUserDetails details) {
        List<String> roles = details.getRoleNames().stream().sorted().toList();
        List<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        return new MeResponse(
                details.getUsername(),
                details.getDepartment(),
                details.getClearanceLevel(),
                roles,
                authorities);
    }
}
