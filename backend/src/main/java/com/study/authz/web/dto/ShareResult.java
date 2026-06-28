package com.study.authz.web.dto;

/** 공유 액션 인가 결과(학습용 설명 포함). */
public record ShareResult(Long documentId, String sharedWith, String message) {
}
