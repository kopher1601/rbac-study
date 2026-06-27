package com.study.authz.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 역할(예: VIEWER / EDITOR / ADMIN). 다수의 {@link Permission} 을 묶는다.
 *
 * <p>VIEWER&lt;EDITOR&lt;ADMIN 같은 <b>역할 계층</b>은 이 엔티티가 아니라
 * Spring Security 의 {@code RoleHierarchy} 빈으로 코드에 선언한다(SecurityConfig 참고).
 * 단, {@code hasAuthority} 검사를 위해 상위 역할은 하위 역할의 권한을 시드에서 모두 펼쳐 보유한다.
 *
 * <p>{@code permissions} 가 EAGER 인 이유: 로그인 직후 권한(GrantedAuthority) 계산에 즉시 필요하고,
 * 역할당 권한 수가 소수라 비용이 작기 때문. {@code Set} 사용으로 다중 컬렉션 동시 페치 문제(bag)도 피한다.
 */
@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new LinkedHashSet<>();

    public Role(String name) {
        this.name = name;
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }
}
