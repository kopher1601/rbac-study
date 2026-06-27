package com.study.authz.rbac;

import com.study.authz.security.AppUserDetails;
import com.study.authz.web.dto.DecisionDto;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * 현재 유저가 각 문서 액션을 수행할 수 있는지와 그 사유를 계산한다.
 *
 * <p>컨트롤러의 {@code @PreAuthorize("hasAuthority('document:write')")} 와 <b>같은 권한 집합</b>을 보므로
 * 설명과 실제 인가 결정이 항상 일치한다. 이 화면이 RBAC 학습의 핵심: "왜 허용/거부됐는가" 를 드러낸다.
 */
@Service
public class AccessDecisionExplainer {

    public List<DecisionDto> explainDocumentActions(AppUserDetails user) {
        Set<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return List.of(DocumentAction.values()).stream()
                .map(action -> toDecision(action, authorities))
                .toList();
    }

    private DecisionDto toDecision(DocumentAction action, Set<String> authorities) {
        boolean allowed = authorities.contains(action.requiredAuthority());
        String reason = allowed
                ? "보유 권한 '" + action.requiredAuthority() + "' → " + action.label() + " 허용"
                : "권한 '" + action.requiredAuthority() + "' 없음 → " + action.label() + " 거부";
        return new DecisionDto(action.name(), action.label(), allowed, action.requiredAuthority(), reason);
    }
}
