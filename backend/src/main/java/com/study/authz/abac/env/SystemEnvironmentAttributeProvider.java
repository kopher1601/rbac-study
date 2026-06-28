package com.study.authz.abac.env;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/** 운영 기본 구현 — 시스템 시계를 사용한다. 테스트에서는 {@code @MockitoBean} 으로 대체된다. */
@Component
public class SystemEnvironmentAttributeProvider implements EnvironmentAttributeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
