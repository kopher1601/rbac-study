package com.study.authz.web.dto;

import jakarta.validation.constraints.NotBlank;

/** 문서 공유 요청. RBAC 단계에서는 인가만 확인하고 실제 관계 저장은 Stage 3(ReBAC)에서 구현한다. */
public record ShareRequest(@NotBlank String targetUsername) {
}
