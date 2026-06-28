package com.study.authz.security;

import com.study.authz.domain.User;
import com.study.authz.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * formLogin 이 사용하는 사용자 조회. EAGER 로딩된 역할/권한이 트랜잭션 안에서 초기화되도록
 * {@code @Transactional(readOnly = true)} 로 감싼다.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        return new AppUserDetails(user);
    }
}
