package com.study.authz.web.dto;

import com.study.authz.abac.model.RuleEvaluation;

/** 규칙 한 건의 평가를 화면에 보여주기 위한 표현(프런트 why 패널의 한 행). */
public record RuleTraceDto(
        String ruleId,
        String description,
        String effect,
        boolean applicable,
        String reason) {

    public static RuleTraceDto from(RuleEvaluation evaluation) {
        return new RuleTraceDto(
                evaluation.ruleId(),
                evaluation.description(),
                evaluation.effect().name(),
                evaluation.applicable(),
                evaluation.reason());
    }
}
