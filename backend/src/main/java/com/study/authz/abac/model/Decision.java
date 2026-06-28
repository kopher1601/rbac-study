package com.study.authz.abac.model;

import java.util.List;

/**
 * PDP({@code PolicyDecisionPoint}) 의 최종 결정.
 *
 * <p>enforcement(컨트롤러의 {@code @PreAuthorize("@abacAccess...")}) 와 explanation(설명 엔드포인트)이
 * <b>같은 Decision</b> 을 보므로 RBAC 의 {@code AccessDecisionExplainer} 처럼 "설명=실제" 가 보장된다.
 *
 * @param permitted   최종 허용 여부
 * @param matchedRule 결정을 내린 규칙 id(거부면 첫 DENY 규칙, 허용이면 첫 PERMIT 규칙, 둘 다 없으면 {@code default-deny})
 * @param summary     한 줄 한국어 요약
 * @param trace       적용 가능 여부와 무관하게 모든 규칙의 평가 기록(프런트 why 패널용)
 */
public record Decision(
        boolean permitted,
        String matchedRule,
        String summary,
        List<RuleEvaluation> trace) {
}
