package com.study.authz.abac;

import com.study.authz.abac.model.Decision;
import org.springframework.stereotype.Component;

/**
 * {@code @PreAuthorize} 의 SpEL 진입점. 컨트롤러는
 * {@code @PreAuthorize("@abacAccess.canWriteDocument(#id)")} 처럼 이 빈을 호출한다.
 *
 * <p><b>왜 이 방식인가</b>: 커스텀 {@code PermissionEvaluator}/{@code MethodSecurityExpressionHandler} 를
 * 도입하면(= {@code hasPermission(...)} 관용구) Spring Security 7 에서 {@code RoleHierarchy} 자동 주입이
 * 끊겨 기존 RBAC 역할 계층이 깨진다(검증됨). SpEL 빈 참조는 메서드 보안 설정을 전혀 건드리지 않아
 * 안전하고, "애너테이션이 리소스 id 를 받아 그 리소스의 속성을 본다" 는 ABAC 의 전환을 가장 선명히 드러낸다.
 *
 * <p>enforcement(여기)와 explanation(서비스의 {@code explain*})이 <b>같은 PDP</b>를 호출하므로 설명=실제.
 */
@Component("abacAccess")
public class AbacAccess {

    private final AbacRequestFactory requestFactory;
    private final PolicyDecisionPoint pdp;

    public AbacAccess(AbacRequestFactory requestFactory, PolicyDecisionPoint pdp) {
        this.requestFactory = requestFactory;
        this.pdp = pdp;
    }

    public boolean canReadDocument(Long id) {
        return permittedDocument(id, AbacAction.READ);
    }

    public boolean canWriteDocument(Long id) {
        return permittedDocument(id, AbacAction.WRITE);
    }

    public boolean canDeleteDocument(Long id) {
        return permittedDocument(id, AbacAction.DELETE);
    }

    public boolean canShareDocument(Long id) {
        return permittedDocument(id, AbacAction.SHARE);
    }

    public boolean canReadFolder(Long id) {
        return permittedFolder(id, AbacAction.READ);
    }

    public boolean canWriteFolder(Long id) {
        return permittedFolder(id, AbacAction.WRITE);
    }

    private boolean permittedDocument(Long id, AbacAction action) {
        Decision decision = pdp.decide(requestFactory.forDocument(id, action));
        return decision.permitted();
    }

    private boolean permittedFolder(Long id, AbacAction action) {
        Decision decision = pdp.decide(requestFactory.forFolder(id, action));
        return decision.permitted();
    }
}
