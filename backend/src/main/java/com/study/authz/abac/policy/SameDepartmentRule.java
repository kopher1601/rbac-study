package com.study.authz.abac.policy;

import com.study.authz.abac.AbacAction;
import com.study.authz.abac.model.AccessRequest;
import com.study.authz.abac.model.RuleEvaluation;
import java.util.Objects;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 같은 부서 규칙: 주체 부서가 리소스 소유 부서와 같아야 한다(소유자 면제).
 * 리소스에는 부서 컬럼이 없으므로 {@code ownerDepartment}(소유자의 부서)와 비교한다.
 *
 * <p>RBAC 로 같은 효과를 내려면 "(부서 × 액션)" 마다 스코프 역할을 만들어야 해 역할이 폭증한다
 * (Stage 1 의 역할 폭발 데모). ABAC 는 속성 비교 규칙 하나로 대체한다.
 */
@Component
@Order(3)
public class SameDepartmentRule implements PolicyRule {

    @Override
    public String id() {
        return "same-department";
    }

    @Override
    public String description() {
        return "주체 부서가 리소스 소유 부서와 같아야 함(소유자 면제)";
    }

    @Override
    public boolean appliesTo(AbacAction action) {
        return true;
    }

    @Override
    public RuleEvaluation evaluate(AccessRequest request) {
        if (request.subjectIsOwner()) {
            return RuleEvaluation.notApplicable(id(), description(), "소유자이므로 부서 규칙 면제");
        }
        String subjectDept = request.subject().department();
        String ownerDept = request.resource().ownerDepartment();
        if (Objects.equals(subjectDept, ownerDept)) {
            return RuleEvaluation.permit(id(), description(),
                    "부서 일치(" + subjectDept + " == " + ownerDept + ") → 허용");
        }
        return RuleEvaluation.deny(id(), description(),
                "부서 불일치(" + subjectDept + " ≠ " + ownerDept + ") → 거부");
    }
}
