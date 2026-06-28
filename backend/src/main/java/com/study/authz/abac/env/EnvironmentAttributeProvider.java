package com.study.authz.abac.env;

import java.time.LocalDateTime;

/**
 * 환경 속성(현재 시각)의 공급원. 시간을 이 한 점으로 추상화해 두면, 테스트에서
 * {@code @MockitoBean} 으로 {@code now()} 만 바꿔 업무시간/시간외 시나리오를 결정적으로 만들 수 있다.
 *
 * <p>업무시간 판정 로직 자체는 여기 두지 않고 {@code Environment.at(now)} 에 응집한다 —
 * 그래야 목킹이 {@code now()} 한 메서드로 끝나고(default 메서드를 목이 덮어쓰는 함정 회피),
 * 업무시간 계산은 실제 경로로 검증된다.
 */
public interface EnvironmentAttributeProvider {

    LocalDateTime now();
}
