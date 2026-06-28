package com.study.authz.web.dto;

import com.study.authz.abac.AbacAction;
import com.study.authz.abac.model.Decision;
import java.util.List;

/**
 * 한 리소스에 대한 한 액션의 ABAC 결정 + 전체 규칙 trace.
 * RBAC 의 {@code DecisionDto}(액션×권한) 와 달리, ABAC 는 결정이 리소스 속성에 의존하므로
 * 리소스마다·액션마다 산출되고 규칙별 평가 근거(trace)를 함께 싣는다.
 */
public record AbacDecisionDto(
        String action,
        String label,
        boolean permitted,
        String matchedRule,
        String summary,
        List<RuleTraceDto> trace) {

    public static AbacDecisionDto from(AbacAction action, Decision decision) {
        return new AbacDecisionDto(
                action.name(),
                action.label(),
                decision.permitted(),
                decision.matchedRule(),
                decision.summary(),
                decision.trace().stream().map(RuleTraceDto::from).toList());
    }
}
