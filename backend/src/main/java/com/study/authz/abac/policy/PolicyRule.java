package com.study.authz.abac.policy;

import com.study.authz.abac.AbacAction;
import com.study.authz.abac.model.AccessRequest;
import com.study.authz.abac.model.RuleEvaluation;

/**
 * 정책 규칙 한 건. ABAC 를 "if 문 뭉치" 가 아니라 <b>정책 객체의 모음</b>으로 표현하기 위한 단위다.
 *
 * <p>각 규칙은 {@code @Component} 로 등록되고 Spring 이 {@code List<PolicyRule>} 로 PDP 에 주입한다.
 * 새 정책을 추가하려면 파일 하나(새 {@code @Component})만 더하면 되며, PDP·컨트롤러는 건드릴 필요가 없다.
 *
 * <p>주입 순서(= trace 표시 순서, deny-overrides 의 "첫 DENY/PERMIT" 판정 순서)는 구현체의
 * {@code @Order} 로 고정한다.
 */
public interface PolicyRule {

    /** 규칙 식별자(예: {@code clearance-dominance}). matchedRule 로도 쓰인다. */
    String id();

    /** 규칙의 의도(사람이 읽는 한 줄). */
    String description();

    /** 이 규칙이 해당 액션에 적용되는가(예: 업무시간 규칙은 READ 에 적용되지 않음). */
    boolean appliesTo(AbacAction action);

    /** {@link #appliesTo(AbacAction)} 가 true 일 때만 호출된다. */
    RuleEvaluation evaluate(AccessRequest request);
}
