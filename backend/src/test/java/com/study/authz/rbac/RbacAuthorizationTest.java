package com.study.authz.rbac;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * RBAC 허용/거부 매트릭스 + 역할 계층 통합 테스트.
 *
 * <p>{@code @WithUserDetails} 로 실제 시드 유저를 {@code AppUserDetailsService} 가 로드하므로
 * 진짜 권한 파생(ROLE_*+권한문자열)과 {@code RoleHierarchy} 연결까지 함께 검증된다.
 * {@code @Transactional} 로 각 테스트의 DB 변경(생성/삭제)은 롤백되어 격리된다.
 */
@SpringBootTest
@Transactional
class RbacAuthorizationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    // 시드(DataSeeder)는 컨텍스트 시작 시 1회 실행되어 doc 1~3 이 존재한다.

    @BeforeEach
    void setUp() {
        // Boot 4 에서는 @AutoConfigureMockMvc 가 springSecurity() 의 테스트 SecurityContext 브리지를
        // 자동 적용하지 않으므로(@WithUserDetails 가 전달 안 됨), 직접 springSecurity() 를 붙여 빌드한다.
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithUserDetails("carol") // VIEWER
    void viewer_가_문서를_읽을_수_있다() throws Exception {
        mvc.perform(get("/api/rbac/documents")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("carol") // VIEWER
    void viewer_는_문서를_생성할_수_없다_403() throws Exception {
        mvc.perform(post("/api/rbac/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"sensitivityLevel\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("bob") // EDITOR
    void editor_는_문서를_생성할_수_있다_201() throws Exception {
        mvc.perform(post("/api/rbac/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"새 문서\",\"sensitivityLevel\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithUserDetails("bob") // EDITOR
    void editor_는_문서를_삭제할_수_없다_403() throws Exception {
        mvc.perform(delete("/api/rbac/documents/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("alice") // ADMIN
    void admin_은_문서를_삭제할_수_있다_204() throws Exception {
        mvc.perform(delete("/api/rbac/documents/1")).andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("alice") // ADMIN
    void admin_은_문서를_공유할_수_있다_200() throws Exception {
        mvc.perform(post("/api/rbac/documents/2/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUsername\":\"bob\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("alice") // ADMIN — hasRole('VIEWER') 엔드포인트를 계층으로 통과
    void 역할계층_admin_은_viewer_역할_엔드포인트를_통과한다() throws Exception {
        mvc.perform(get("/api/rbac/documents")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("bob") // EDITOR — 역시 계층으로 VIEWER 통과
    void 역할계층_editor_는_viewer_역할_엔드포인트를_통과한다() throws Exception {
        mvc.perform(get("/api/rbac/documents")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("carol") // VIEWER
    void viewer_의_결정설명은_read만_허용이다() throws Exception {
        // 결정 순서는 DocumentAction.values() 고정: [0]READ [1]WRITE [2]DELETE [3]SHARE
        mvc.perform(get("/api/rbac/documents/decisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("READ"))
                .andExpect(jsonPath("$[0].allowed").value(true))
                .andExpect(jsonPath("$[1].action").value("WRITE"))
                .andExpect(jsonPath("$[1].allowed").value(false))
                .andExpect(jsonPath("$[2].action").value("DELETE"))
                .andExpect(jsonPath("$[2].allowed").value(false));
    }
}
