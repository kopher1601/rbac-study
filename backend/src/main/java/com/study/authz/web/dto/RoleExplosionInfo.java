package com.study.authz.web.dto;

/**
 * 역할 폭발(Role Explosion) 지표. 프런트 슬라이더 시뮬레이터의 입력값으로 쓰인다.
 *
 * @param departments              현재 스코프 역할이 다루는 부서 수
 * @param actionTiers              액션 등급(=전역 역할) 수 (VIEWER/EDITOR/ADMIN)
 * @param rolesNeededForFullScoping 부서 × 등급을 모두 커버하려면 필요한 스코프 역할 수
 * @param scopedRolesSeeded        실제로 시드된 스코프 역할 수
 * @param note                     학습용 설명
 */
public record RoleExplosionInfo(
        int departments,
        int actionTiers,
        int rolesNeededForFullScoping,
        int scopedRolesSeeded,
        String note) {
}
