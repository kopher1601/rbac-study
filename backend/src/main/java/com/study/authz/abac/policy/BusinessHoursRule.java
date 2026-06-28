package com.study.authz.abac.policy;

import com.study.authz.abac.AbacAction;
import com.study.authz.abac.model.AccessRequest;
import com.study.authz.abac.model.RuleEvaluation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 업무시간(환경) 규칙: 쓰기/삭제/공유는 평일 09–18시에만 허용한다. READ 에는 적용되지 않는다.
 *
 * <p>이 규칙이 ABAC 와 RBAC 를 가르는 결정적 지점이다 — 주체의 역할·권한·속성이 하나도 변하지 않아도
 * <b>시간</b>만으로 결과가 바뀐다. 소유자에게도 적용되므로, 소유자가 자기 문서를 업무시간 외에 수정하려 하면
 * deny-overrides 로 거부된다(환경이 소유를 이긴다). RBAC 는 이런 컨텍스트 조건을 표현할 수단이 없다.
 */
@Component
@Order(4)
public class BusinessHoursRule implements PolicyRule {

    @Override
    public String id() {
        return "business-hours";
    }

    @Override
    public String description() {
        return "쓰기/삭제/공유는 업무시간(평일 09–18시)에만 허용";
    }

    @Override
    public boolean appliesTo(AbacAction action) {
        return action.businessHoursControlled();
    }

    @Override
    public RuleEvaluation evaluate(AccessRequest request) {
        if (request.environment().businessHours()) {
            return RuleEvaluation.notApplicable(id(), description(),
                    request.environment().dayOfWeek() + " "
                            + request.environment().now().getHour() + "시 → 업무시간 내(통과)");
        }
        return RuleEvaluation.deny(id(), description(),
                request.environment().dayOfWeek() + " "
                        + request.environment().now().getHour() + "시 → 업무시간 외, 쓰기/삭제/공유 금지(소유자도 적용)");
    }
}
