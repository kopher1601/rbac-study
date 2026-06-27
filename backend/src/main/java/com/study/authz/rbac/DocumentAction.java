package com.study.authz.rbac;

/**
 * 문서에 대한 RBAC 액션과 그에 필요한 권한(authority)의 매핑.
 *
 * <p>여기 권한 문자열은 컨트롤러의 {@code @PreAuthorize("hasAuthority('document:write')")} 와
 * 동일한 값이라, {@link AccessDecisionExplainer} 의 설명이 실제 인가 결정과 일치한다.
 * (애너테이션은 컴파일 타임 상수만 받으므로 같은 문자열을 양쪽에 둔다.)
 */
public enum DocumentAction {

    READ("document:read", "문서 읽기"),
    WRITE("document:write", "문서 수정"),
    DELETE("document:delete", "문서 삭제"),
    SHARE("document:share", "문서 공유");

    private final String requiredAuthority;
    private final String label;

    DocumentAction(String requiredAuthority, String label) {
        this.requiredAuthority = requiredAuthority;
        this.label = label;
    }

    public String requiredAuthority() {
        return requiredAuthority;
    }

    public String label() {
        return label;
    }
}
