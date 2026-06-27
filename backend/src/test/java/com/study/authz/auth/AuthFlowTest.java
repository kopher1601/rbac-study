package com.study.authz.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/** 인증/세션 플로우 통합 테스트(formLogin + 세션 지속). */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void 미인증_me_는_401() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void 유저목록은_공개다_200() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void 로그인_후_같은_세션의_me_는_현재유저를_반환한다() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/login")
                        .param("username", "alice")
                        .param("password", "password")
                        .session(session))
                .andExpect(status().isOk());

        mvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.authorities", org.hamcrest.Matchers.hasItem("document:delete")));
    }

    @Test
    void 오답_로그인은_401() throws Exception {
        mvc.perform(post("/api/login")
                        .param("username", "alice")
                        .param("password", "WRONG"))
                .andExpect(status().isUnauthorized());
    }
}
