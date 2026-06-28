package com.study.authz.abac.policy;

import com.study.authz.abac.AbacAction;
import com.study.authz.abac.model.AccessRequest;
import com.study.authz.abac.model.RuleEvaluation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 등급 우월 규칙(Bell–LaPadula 류 "no read up"): 주체 등급이 리소스 민감도 이상이어야 한다.
 * 통과 시 PERMIT 한 표, 미달 시 DENY. 소유자는 면제(자기 문서는 등급과 무관히 접근).
 *
 * <p>RBAC 가 표현 못 하는 핵심: 같은 유저라도 <b>문서의 민감도</b>에 따라 결과가 갈린다(per-resource).
 */
@Component
@Order(2)
public class ClearanceDominanceRule implements PolicyRule {

    @Override
    public String id() {
        return "clearance-dominance";
    }

    @Override
    public String description() {
        return "주체 보안등급이 리소스 민감도 이상이어야 함(소유자 면제)";
    }

    @Override
    public boolean appliesTo(AbacAction action) {
        return true;
    }

    @Override
    public RuleEvaluation evaluate(AccessRequest request) {
        if (request.subjectIsOwner()) {
            return RuleEvaluation.notApplicable(id(), description(), "소유자이므로 등급 규칙 면제");
        }
        int clearance = request.subject().clearanceLevel();
        int sensitivity = request.resource().sensitivityLevel();
        if (clearance >= sensitivity) {
            return RuleEvaluation.permit(id(), description(),
                    "clearance " + clearance + " ≥ sensitivity " + sensitivity + " → 등급 충족");
        }
        return RuleEvaluation.deny(id(), description(),
                "clearance " + clearance + " < sensitivity " + sensitivity + " → 등급 부족");
    }
}
