package com.study.authz.abac.model;

import java.util.List;

/**
 * 주체(인증 유저) 속성. {@code AppUserDetails} 스냅샷에서 추출하므로 LAZY 로딩이 없다.
 *
 * @param userId         유저 id(소유 비교용)
 * @param username       유저명
 * @param department     부서(같은 부서 규칙용)
 * @param clearanceLevel 보안 등급(등급≥민감도 규칙용)
 * @param roleNames      역할명(참고용 — ABAC 는 역할에 의존하지 않지만 화면 표시에 쓸 수 있다)
 */
public record Subject(
        Long userId,
        String username,
        String department,
        int clearanceLevel,
        List<String> roleNames) {
}
