package com.study.authz.abac.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * 환경(컨텍스트) 속성. ABAC 가 RBAC 와 갈리는 결정적 차원 — 주체·리소스가 그대로여도
 * <b>시간</b>에 따라 결정이 바뀐다.
 *
 * <p>업무시간 정의(평일 09–18시)는 {@link #at(LocalDateTime)} 한 곳에 응집한다. 이렇게 하면
 * 테스트는 시간 제공자({@code EnvironmentAttributeProvider})의 {@code now()} 만 목킹하면 되고,
 * 업무시간 계산은 실제 코드 경로 그대로 검증된다.
 *
 * @param now           평가 시점
 * @param businessHours 업무시간 여부(평일 09:00–18:00)
 * @param dayOfWeek     요일(설명용)
 */
public record Environment(LocalDateTime now, boolean businessHours, DayOfWeek dayOfWeek) {

    public static Environment at(LocalDateTime now) {
        DayOfWeek day = now.getDayOfWeek();
        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        boolean inHours = now.getHour() >= 9 && now.getHour() < 18;
        return new Environment(now, weekday && inHours, day);
    }
}
