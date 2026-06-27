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
 * 사용자. {@code department}/{@code clearanceLevel} 은 Stage 2(ABAC) 에서 쓸 속성을 미리 보유한다.
 * 인증은 학습 단순화를 위해 시드 유저 + 공통 비밀번호(formLogin) 방식이다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private int clearanceLevel;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    public User(String username, String passwordHash, String department, int clearanceLevel) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.department = department;
        this.clearanceLevel = clearanceLevel;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }
}
