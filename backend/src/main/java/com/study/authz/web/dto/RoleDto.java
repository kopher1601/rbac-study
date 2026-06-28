package com.study.authz.web.dto;

import java.util.List;

/** 역할 한 줄(스코프 역할 여부 포함). */
public record RoleDto(String name, boolean scoped, List<String> permissions) {
}
