package com.study.authz.abac;

/**
 * ABAC 인가 액션. RBAC 의 {@code DocumentAction} 과 같은 액션 집합이지만, ABAC 에서는
 * "어떤 권한(authority)이 필요한가" 대신 <b>정책 규칙들이 액션별로 다르게 적용</b>된다.
 *
 * <p>{@code businessHoursControlled} 은 환경(업무시간) 규칙의 적용 여부를 액션별로 가른다.
 * 읽기(READ)는 24시간 허용, 쓰기/삭제/공유는 업무시간에만 허용 — 같은 주체라도 <b>시간에 따라</b>
 * 결과가 갈리는 ABAC 의 환경 속성을 보여준다(RBAC 로는 절대 표현 못 하는 차별점).
 */
public enum AbacAction {

    READ("읽기", false),
    WRITE("수정", true),
    DELETE("삭제", true),
    SHARE("공유", true);

    private final String label;
    private final boolean businessHoursControlled;

    AbacAction(String label, boolean businessHoursControlled) {
        this.label = label;
        this.businessHoursControlled = businessHoursControlled;
    }

    public String label() {
        return label;
    }

    /** 이 액션이 업무시간 규칙의 적용 대상인지. */
    public boolean businessHoursControlled() {
        return businessHoursControlled;
    }
}
