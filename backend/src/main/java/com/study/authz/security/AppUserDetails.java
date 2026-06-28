package com.study.authz.security;

import com.study.authz.domain.Permission;
import com.study.authz.domain.Role;
import com.study.authz.domain.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security 가 쓰는 인증 주체. {@link User} 엔티티를 직접 들고 있지 않고
 * 로그인 시점에 필요한 값만 <b>스냅샷</b>으로 복사한다(세션에 detached 엔티티 그래프를 담지 않기 위함).
 *
 * <p>GrantedAuthority 는 두 가지로 노출한다:
 * <ul>
 *   <li>역할 → {@code ROLE_<name>} ({@code hasRole(...)} 용, ROLE_ 접두사 필수)
 *   <li>권한 → {@code document:write} 같은 원문 ({@code hasAuthority(...)} 용)
 * </ul>
 */
public class AppUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String passwordHash;
    private final String department;
    private final int clearanceLevel;
    private final List<String> roleNames;
    private final List<GrantedAuthority> authorities;

    public AppUserDetails(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.department = user.getDepartment();
        this.clearanceLevel = user.getClearanceLevel();

        List<String> names = new ArrayList<>();
        Set<GrantedAuthority> auths = new LinkedHashSet<>();
        for (Role role : user.getRoles()) {
            names.add(role.getName());
            auths.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission permission : role.getPermissions()) {
                auths.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        this.roleNames = List.copyOf(names);
        this.authorities = List.copyOf(auths);
    }

    public Long getUserId() {
        return userId;
    }

    public String getDepartment() {
        return department;
    }

    public int getClearanceLevel() {
        return clearanceLevel;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
