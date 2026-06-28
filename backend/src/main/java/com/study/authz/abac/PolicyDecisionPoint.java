package com.study.authz.abac;

import com.study.authz.abac.model.AccessRequest;
import com.study.authz.abac.model.Decision;
import com.study.authz.abac.model.Effect;
import com.study.authz.abac.model.RuleEvaluation;
import com.study.authz.abac.policy.PolicyRule;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * PDP(Policy Decision Point) — 정책 규칙들을 평가해 최종 인가 결정을 내린다.
 *
 * <p><b>조합 알고리즘: deny-overrides</b> (XACML 표준 조합자)
 * <ol>
 *   <li>적용 가능한 규칙 중 하나라도 DENY → 거부(보안 fail-safe, 첫 DENY 규칙이 matchedRule).
 *   <li>DENY 가 없고 PERMIT 가 하나 이상 → 허용(첫 PERMIT 규칙이 matchedRule).
 *   <li>둘 다 없음(전부 NOT_APPLICABLE) → 기본 거부(closed-world).
 * </ol>
 *
 * <p>단락(short-circuit)하지 않고 <b>모든 규칙을 끝까지 평가</b>해 {@code trace} 에 전부 담는다 —
 * 프런트가 "owner 는 통과했지만 업무시간에서 막혔다" 처럼 거부 사유를 동시에 보여줄 수 있게 하기 위함.
 * 규칙 순서는 각 규칙의 {@code @Order} 로 고정되어 matchedRule 이 결정적이다.
 */
@Service
public class PolicyDecisionPoint {

    private final List<PolicyRule> rules;

    /** Spring 이 모든 {@link PolicyRule} 빈을 {@code @Order} 순서로 주입한다. */
    public PolicyDecisionPoint(List<PolicyRule> rules) {
        this.rules = rules;
    }

    public Decision decide(AccessRequest request) {
        List<RuleEvaluation> trace = new ArrayList<>();
        String firstDeny = null;
        String firstDenyReason = null;
        String firstPermit = null;
        String firstPermitReason = null;

        for (PolicyRule rule : rules) {
            if (!rule.appliesTo(request.action())) {
                trace.add(RuleEvaluation.outOfScope(rule.id(), rule.description(),
                        request.action() + " 액션에는 적용되지 않는 규칙"));
                continue;
            }
            RuleEvaluation evaluation = rule.evaluate(request);
            trace.add(evaluation);
            if (evaluation.effect() == Effect.DENY && firstDeny == null) {
                firstDeny = evaluation.ruleId();
                firstDenyReason = evaluation.reason();
            } else if (evaluation.effect() == Effect.PERMIT && firstPermit == null) {
                firstPermit = evaluation.ruleId();
                firstPermitReason = evaluation.reason();
            }
        }

        if (firstDeny != null) {
            return new Decision(false, firstDeny, "거부 — " + firstDenyReason, trace);
        }
        if (firstPermit != null) {
            return new Decision(true, firstPermit, "허용 — " + firstPermitReason, trace);
        }
        return new Decision(false, "default-deny",
                "거부 — 명시적으로 허용한 정책이 없음(기본 거부)", trace);
    }
}
