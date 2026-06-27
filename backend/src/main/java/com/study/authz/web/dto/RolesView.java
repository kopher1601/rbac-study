package com.study.authz.web.dto;

import java.util.List;

/** 역할-권한 관리 화면 데이터. */
public record RolesView(
        int roleCount,
        int permissionCount,
        List<RoleDto> roles,
        RoleExplosionInfo explosion) {
}
