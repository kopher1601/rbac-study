package com.study.authz.web.dto;

/**
 * "이 액션을 할 수 있는가 + 왜" 를 담는 학습용 결정 설명.
 * 인가 자체를 우회하지 않고, 현재 유저의 권한으로 결과만 미리 계산해 보여준다.
 */
public record DecisionDto(String action, String label, boolean allowed, String requiredAuthority, String reason) {
}
