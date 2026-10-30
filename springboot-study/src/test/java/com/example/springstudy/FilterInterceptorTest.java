package com.example.springstudy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证过滤器 RequestIdFilter 和拦截器 AccessLogInterceptor 都已生效。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class FilterInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void filterGeneratesRequestIdWhenClientSendsNone() throws Exception {
        String requestId = mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader("X-Request-Id");

        assertThat(requestId).isNotBlank().hasSize(36);
    }

    @Test
    void filterKeepsSafeRequestIdFromClient() throws Exception {
        mockMvc.perform(get("/api/accounts").header("X-Request-Id", "client-123"))
                .andExpect(header().string("X-Request-Id", "client-123"));
    }

    @Test
    void filterReplacesUnsafeRequestId() throws Exception {
        String requestId = mockMvc.perform(get("/api/accounts").header("X-Request-Id", "bad id!"))
                .andReturn().getResponse().getHeader("X-Request-Id");

        assertThat(requestId).isNotEqualTo("bad id!").hasSize(36);
    }

    @Test
    void filterAlsoCoversRequestsThatNeverReachController() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void interceptorLogsStatusAndHandlerWithRequestId(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/api/accounts").header("X-Request-Id", "trace-abc"));

        assertThat(output.getOut())
                .contains("接口访问：GET /api/accounts", "AccountController.listAccounts()", "状态码=200")
                .contains("[trace-abc]");
    }

    @Test
    void interceptorLogsValidationFailureThatAspectCannotSee(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/api/accounts/0"));

        assertThat(output.getOut()).contains("接口访问：GET /api/accounts/0", "状态码=400");
    }
}
