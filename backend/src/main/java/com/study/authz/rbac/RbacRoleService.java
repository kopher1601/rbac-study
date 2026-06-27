package com.study.authz.rbac;

import com.study.authz.domain.Permission;
import com.study.authz.domain.Role;
import com.study.authz.domain.repository.PermissionRepository;
import com.study.authz.domain.repository.RoleRepository;
import com.study.authz.web.dto.RoleDto;
import com.study.authz.web.dto.RoleExplosionInfo;
import com.study.authz.web.dto.RolesView;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 역할-권한 매트릭스와 역할 폭발 지표를 계산한다.
 *
 * <p>스코프 역할({@code <DEPT>_FOLDER_EDITOR})은 "특정 부서에서만 편집" 을 표현하려고 만든 것.
 * 순수 RBAC 에선 부서 × 액션 등급마다 별도 역할이 필요해 역할 수가 곱으로 폭증한다.
 */
@Service
public class RbacRoleService {

    private static final String SCOPED_MARKER = "_FOLDER_";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RbacRoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public RolesView overview() {
        List<Role> roles = roleRepository.findAll();

        List<RoleDto> roleDtos = roles.stream()
                .sorted(Comparator.comparing(Role::getName))
                .map(this::toRoleDto)
                .toList();

        RoleExplosionInfo explosion = computeExplosion(roles);
        return new RolesView(roleDtos.size(), (int) permissionRepository.count(), roleDtos, explosion);
    }

    private RoleDto toRoleDto(Role role) {
        boolean scoped = role.getName().contains(SCOPED_MARKER);
        List<String> permissions = role.getPermissions().stream()
                .map(Permission::getName)
                .sorted()
                .toList();
        return new RoleDto(role.getName(), scoped, permissions);
    }

    private RoleExplosionInfo computeExplosion(List<Role> roles) {
        // 데이터 기반: 스코프 역할 이름에서 부서를, 전역 역할 수에서 액션 등급을 도출
        Set<String> departments = roles.stream()
                .map(Role::getName)
                .filter(name -> name.contains(SCOPED_MARKER))
                .map(name -> name.substring(0, name.indexOf(SCOPED_MARKER)))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int actionTiers = (int) roles.stream()
                .filter(role -> !role.getName().contains(SCOPED_MARKER))
                .count();
        int scopedSeeded = (int) roles.stream()
                .filter(role -> role.getName().contains(SCOPED_MARKER))
                .count();
        int needed = departments.size() * actionTiers;

        String note = "순수 RBAC 은 '특정 부서/폴더에서만 X' 를 역할 하나로 표현하지 못한다. "
                + "부서 " + departments.size() + "개 × 액션 등급 " + actionTiers + "개 = " + needed
                + "개의 스코프 역할이 필요하고, 부서/폴더가 늘면 곱으로 폭증한다(Role Explosion). "
                + "Stage 2(ABAC)에서는 '같은 부서면 편집' 같은 속성 규칙 1개로 이를 대체한다.";

        return new RoleExplosionInfo(departments.size(), actionTiers, needed, scopedSeeded, note);
    }
}
