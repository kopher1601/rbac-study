package com.study.authz.web;

import com.study.authz.rbac.RbacRoleService;
import com.study.authz.web.dto.RolesView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 역할-권한 관리/조회 화면용 API. 어떤 유저로 전환해 보더라도 매트릭스와 역할 폭발 지표를
 * 볼 수 있도록 인증만 요구한다(특정 역할 강제 안 함 — URL 규칙의 authenticated() 적용).
 */
@RestController
@RequestMapping("/api/rbac/roles")
public class RbacAdminController {

    private final RbacRoleService roleService;

    public RbacAdminController(RbacRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public RolesView overview() {
        return roleService.overview();
    }
}
