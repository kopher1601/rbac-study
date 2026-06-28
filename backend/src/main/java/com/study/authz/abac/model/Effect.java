package com.study.authz.abac.model;

/**
 * 규칙 한 건의 평가 결과.
 *
 * <ul>
 *   <li>{@code PERMIT} — 이 규칙이 허용에 한 표를 던짐
 *   <li>{@code DENY} — 이 규칙이 거부(deny-overrides 조합에서 하나라도 있으면 최종 거부)
 *   <li>{@code NOT_APPLICABLE} — 이 규칙은 이 요청에 관여하지 않음(예: 소유자 면제, 또는 통과 게이트)
 * </ul>
 */
public enum Effect {
    PERMIT,
    DENY,
    NOT_APPLICABLE
}
