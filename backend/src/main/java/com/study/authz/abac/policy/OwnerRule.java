package com.study.authz.abac.policy;

import com.study.authz.abac.AbacAction;
import com.study.authz.abac.model.AccessRequest;
import com.study.authz.abac.model.RuleEvaluation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 소유 규칙: 소유자는 본인 리소스 접근에 한 표(PERMIT)를 던진다.
 *
 * <p>등급·부서 규칙은 소유자를 면제하지만(아래 두 규칙 참고), <b>업무시간 규칙은 소유자에게도 적용</b>되므로
 * deny-overrides 조합에서 "소유자여도 업무시간 외 쓰기는 거부" 가 성립한다 — 환경 규칙이 소유를 이기는 장면.
 */
@Component
@Order(1)
public class OwnerRule implements PolicyRule {

    @Override
    public String id() {
        return "owner-rule";
    }

    @Override
    public String description() {
        return "소유자는 본인 리소스에 접근 허용";
    }

    @Override
    public boolean appliesTo(AbacAction action) {
        return true;
    }

    @Override
    public RuleEvaluation evaluate(AccessRequest request) {
        Long subjectId = request.subject().userId();
        Long ownerId = request.resource().ownerId();
        if (request.subjectIsOwner()) {
            return RuleEvaluation.permit(id(), description(),
                    "소유자 본인(userId=" + subjectId + " == ownerId=" + ownerId + ") → 허용");
        }
        return RuleEvaluation.notApplicable(id(), description(),
                "소유자 아님(userId=" + subjectId + " ≠ ownerId=" + ownerId + ") → 다른 규칙에 위임");
    }
}
