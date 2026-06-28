package com.study.authz.abac;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.study.authz.abac.env.EnvironmentAttributeProvider;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * ABAC 허용/거부 매트릭스 통합 테스트. RBAC 테스트와 같은 패턴
 * ({@code webAppContextSetup + springSecurity + @WithUserDetails})으로 실제 시드 유저 속성을 파생한다.
 *
 * <p>시간은 {@code @MockitoBean} 으로 {@link EnvironmentAttributeProvider#now()} 만 고정한다 —
 * 업무시간 판정은 실제 코드({@code Environment.at})를 그대로 탄다. 기본은 업무시간(월 14시),
 * 시간외 케이스만 메서드에서 22시로 재설정한다.
 *
 * <p>시드(고정): 문서 1=API Design(owner bob/ENG, sens3), 2=Roadmap(owner alice/ENG, sens2),
 * 3=NDA(owner carol/LEGAL, sens4). 폴더 1=Engineering(owner alice/ENG, sens3),
 * 3=Legal(owner carol/LEGAL, sens4). 유저 alice(ENG,5)/bob(ENG,3)/carol(LEGAL,2)/dave(ENG,3).
 */
@SpringBootTest
@Transactional
class AbacAuthorizationTest {

    /** 2026-06-29 은 월요일. 14시 = 업무시간, 22시 = 시간외. */
    private static final LocalDateTime BUSINESS_HOURS = LocalDateTime.of(2026, 6, 29, 14, 0);
    private static final LocalDateTime AFTER_HOURS = LocalDateTime.of(2026, 6, 29, 22, 0);

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private EnvironmentAttributeProvider environmentProvider;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        given(environmentProvider.now()).willReturn(BUSINESS_HOURS);
    }

    // ── 등급(clearance) 규칙 격리: 부서/시간 통제, 등급만 변수 ──────────────────────

    @Test
    @WithUserDetails("bob") // ENG, clearance 3
    void 등급부족이면_고민감_문서_읽기_거부_403() throws Exception {
        // 문서3(NDA, sensitivity 4) — bob clearance 3 < 4 → 거부
        mvc.perform(get("/api/abac/documents/3")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("bob") // ENG, clearance 3
    void 등급충족_같은부서면_읽기_허용_200() throws Exception {
        // 문서2(Roadmap, ENG, sensitivity 2) — bob clearance 3 >= 2, 같은 ENG → 허용
        mvc.perform(get("/api/abac/documents/2")).andExpect(status().isOk());
    }

    // ── 부서(department) 규칙 격리: 등급은 충분하지만 부서로 거부 ────────────────────

    @Test
    @WithUserDetails("alice") // ENG, clearance 5 (충분)
    void 등급충분해도_다른부서면_읽기_거부_403() throws Exception {
        // 문서3(NDA, LEGAL, sensitivity 4) — alice clearance 5 >= 4 지만 부서 ENG ≠ LEGAL → 거부
        mvc.perform(get("/api/abac/documents/3")).andExpect(status().isForbidden());
    }

    // ── 업무시간(환경) 규칙 격리: 같은 주체·문서라도 시간으로 갈림 ───────────────────

    @Test
    @WithUserDetails("carol") // LEGAL, clearance 2, 문서3 소유자
    void 소유자_업무시간_수정_허용_200() throws Exception {
        mvc.perform(put("/api/abac/documents/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"NDA v2\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("carol") // 소유자여도 업무시간 외엔 수정 거부(환경이 소유를 이김)
    void 소유자라도_시간외_수정_거부_403() throws Exception {
        given(environmentProvider.now()).willReturn(AFTER_HOURS);
        mvc.perform(put("/api/abac/documents/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"NDA v2\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("carol") // READ 는 업무시간 규칙 미적용 → 시간외에도 허용
    void 읽기는_시간외에도_허용_200() throws Exception {
        given(environmentProvider.now()).willReturn(AFTER_HOURS);
        mvc.perform(get("/api/abac/documents/3")).andExpect(status().isOk());
    }

    // ── 목록 필터링(RBAC 대조): ABAC 는 행 단위로 거른다 ───────────────────────────

    @Test
    @WithUserDetails("carol") // LEGAL, clearance 2 → 읽을 수 있는 건 본인 소유 문서3 뿐
    void abac_목록은_읽기가능한_문서만_반환한다() throws Exception {
        mvc.perform(get("/api/abac/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    @WithUserDetails("carol") // 같은 유저라도 RBAC 목록은 VIEWER 역할로 전부 반환(대조군)
    void rbac_목록은_전부_반환한다() throws Exception {
        mvc.perform(get("/api/rbac/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    // ── 설명=실제: decisions 의 permitted 와 실제 HTTP 결과가 일치 ──────────────────

    @Test
    @WithUserDetails("alice")
    void 설명_엔드포인트의_거부사유가_실제_403과_일치한다() throws Exception {
        // 결정 설명: 문서3 READ 는 부서 규칙으로 거부
        mvc.perform(get("/api/abac/documents/3/decisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId").value(3))
                .andExpect(jsonPath("$.decisions[0].action").value("READ"))
                .andExpect(jsonPath("$.decisions[0].permitted").value(false))
                .andExpect(jsonPath("$.decisions[0].matchedRule").value("same-department"))
                .andExpect(jsonPath("$.decisions[0].trace[2].ruleId").value("same-department"))
                .andExpect(jsonPath("$.decisions[0].trace[2].effect").value("DENY"));
        // 실제 인가: 같은 문서 READ 시도 → 403 (설명과 일치)
        mvc.perform(get("/api/abac/documents/3")).andExpect(status().isForbidden());
    }

    // ── 폴더도 같은 정책으로 per-resource 인가 ────────────────────────────────────

    @Test
    @WithUserDetails("carol") // LEGAL — Engineering 폴더(owner alice/ENG, sens3)는 부서·등급으로 거부
    void 폴더도_다른부서면_읽기_거부_403() throws Exception {
        mvc.perform(get("/api/abac/folders/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("carol") // 본인 소유 Legal 폴더(id=3)는 허용
    void 소유_폴더는_읽기_허용_200() throws Exception {
        mvc.perform(get("/api/abac/folders/3")).andExpect(status().isOk());
    }
}
