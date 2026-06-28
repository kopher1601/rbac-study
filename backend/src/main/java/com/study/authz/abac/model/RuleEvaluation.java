package com.study.authz.abac.model;

/**
 * 정책 규칙 한 건의 평가 기록. PDP 가 모든 규칙의 평가를 모아 {@link Decision#trace()} 로 보존하며,
 * 프런트의 "왜 허용/거부됐나(why)" 패널이 이 trace 를 규칙별로 펼쳐 보여준다.
 *
 * @param ruleId      규칙 식별자(예: {@code clearance-dominance})
 * @param description 규칙의 의도(사람이 읽는 한 줄)
 * @param effect      평가 결과(PERMIT/DENY/NOT_APPLICABLE)
 * @param applicable  이 액션에 규칙이 적용되는가(false 면 효과는 항상 NOT_APPLICABLE)
 * @param reason      비교한 속성을 그대로 드러내는 서술(예: "clearance 3 < sensitivity 4 → 등급 부족")
 */
public record RuleEvaluation(
        String ruleId,
        String description,
        Effect effect,
        boolean applicable,
        String reason) {

    public static RuleEvaluation permit(String ruleId, String description, String reason) {
        return new RuleEvaluation(ruleId, description, Effect.PERMIT, true, reason);
    }

    public static RuleEvaluation deny(String ruleId, String description, String reason) {
        return new RuleEvaluation(ruleId, description, Effect.DENY, true, reason);
    }

    /** 규칙이 적용되긴 하나 결정에 관여하지 않음(통과/면제). */
    public static RuleEvaluation notApplicable(String ruleId, String description, String reason) {
        return new RuleEvaluation(ruleId, description, Effect.NOT_APPLICABLE, true, reason);
    }

    /** 규칙이 이 액션 자체에 적용되지 않음(예: 업무시간 규칙 × READ). */
    public static RuleEvaluation outOfScope(String ruleId, String description, String reason) {
        return new RuleEvaluation(ruleId, description, Effect.NOT_APPLICABLE, false, reason);
    }
}
