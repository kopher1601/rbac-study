package com.study.authz.abac.model;

/**
 * 리소스(문서/폴더) 속성.
 *
 * <p>문서·폴더 엔티티에는 {@code department} 컬럼이 없으므로, 정책에서 쓰는
 * {@code ownerDepartment} 는 <b>소유자(ownerId)의 부서</b>로 파생한다({@code AbacRequestFactory}).
 *
 * @param type             {@code "document"} 또는 {@code "folder"}
 * @param id               리소스 id
 * @param ownerId          소유자 유저 id(소유 규칙용)
 * @param sensitivityLevel 민감도(등급≥민감도 규칙용)
 * @param ownerDepartment  소유자 부서(같은 부서 규칙용, 파생값)
 */
public record Resource(
        String type,
        Long id,
        Long ownerId,
        int sensitivityLevel,
        String ownerDepartment) {
}
