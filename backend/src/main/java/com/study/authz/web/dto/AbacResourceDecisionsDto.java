package com.study.authz.web.dto;

import java.util.List;

/**
 * 한 리소스(문서/폴더)에 대한 모든 액션의 결정 묶음. 설명 엔드포인트({@code /decisions})의 응답 단위.
 *
 * @param resourceId 리소스 id
 * @param title      문서 제목 또는 폴더 이름(화면 라벨용)
 * @param decisions  액션별 결정(+규칙 trace)
 */
public record AbacResourceDecisionsDto(
        Long resourceId,
        String title,
        List<AbacDecisionDto> decisions) {
}
