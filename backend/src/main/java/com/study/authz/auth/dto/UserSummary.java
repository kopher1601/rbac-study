package com.study.authz.auth.dto;

import com.study.authz.domain.Role;
import com.study.authz.domain.User;
import java.util.List;

/** 유저 스위처용 요약 정보(비밀번호/권한 상세는 제외). */
public record UserSummary(String username, String department, List<String> roles) {

    public static UserSummary from(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).sorted().toList();
        return new UserSummary(user.getUsername(), user.getDepartment(), roles);
    }
}
