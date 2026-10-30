package com.example.springstudy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证 LoginInterceptor：只保护 /api/secure/**，无令牌或令牌错误返回 401，有效令牌放行。
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoginInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/secure/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void rejectsInvalidToken() throws Exception {
        mockMvc.perform(get("/api/secure/profile").header("Authorization", "Bearer wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsHeaderWithoutBearerPrefix() throws Exception {
        mockMvc.perform(get("/api/secure/profile").header("Authorization", "demo-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsValidTokenAndPassesUserToController() throws Exception {
        mockMvc.perform(get("/api/secure/profile").header("Authorization", "Bearer demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("alice"));
    }

    @Test
    void letsPreflightRequestThrough() throws Exception {
        // 预检请求不带令牌，拦截器放行；具体响应由后面的处理逻辑决定，只要不是 401 即可。
        mockMvc.perform(options("/api/secure/profile"))
                .andExpect(result -> {
                    if (result.getResponse().getStatus() == 401) {
                        throw new AssertionError("OPTIONS 预检请求不应被登录拦截器拒绝");
                    }
                });
    }

    @Test
    void doesNotAffectOtherApis() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk());
    }
}
