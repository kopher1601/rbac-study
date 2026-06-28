package com.study.authz.abac.model;

import com.study.authz.abac.AbacAction;

/**
 * 인가 요청 — ABAC 의 네 요소(주체·리소스·액션·환경)를 한데 묶은 PDP 입력.
 * {@code AbacRequestFactory} 가 조립하고 {@code PolicyDecisionPoint} 가 평가한다.
 */
public record AccessRequest(
        Subject subject,
        Resource resource,
        AbacAction action,
        Environment environment) {

    /** 주체가 리소스 소유자인지(여러 규칙이 공유하는 소유 판정). */
    public boolean subjectIsOwner() {
        return subject.userId() != null && subject.userId().equals(resource.ownerId());
    }
}
