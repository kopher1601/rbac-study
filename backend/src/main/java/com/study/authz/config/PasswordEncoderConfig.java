package com.study.authz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 인코더. 시드(DataSeeder)와 로그인(UserDetailsService) 양쪽에서 쓰이므로
 * SecurityConfig 와 분리해 두어 시드만으로도 애플리케이션이 부팅되게 한다.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
