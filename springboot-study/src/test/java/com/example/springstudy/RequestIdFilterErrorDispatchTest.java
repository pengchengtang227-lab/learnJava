package com.example.springstudy;

import com.example.springstudy.filter.RequestIdFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 RequestIdFilter 在容器的 ERROR 分派中也会执行，并沿用第一轮的请求 ID。
 * MockMvc 不会做 ERROR 分派，所以这里直接用 Mock 请求模拟容器的两次进入。
 * OncePerRequestFilter 通过 request 上的 jakarta.servlet.error.request_uri 属性判断是否为错误分派。
 */
class RequestIdFilterErrorDispatchTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void errorDispatchRunsFilterAndReusesRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/does-not-exist");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] seenInChain = new String[2];

        FilterChain firstPass = (req, res) -> seenInChain[0] = MDC.get(RequestIdFilter.MDC_KEY);
        filter.doFilter(request, response, firstPass);

        // 第一轮结束后，容器因 sendError 把同一个请求转发到 /error。
        request.setDispatcherType(DispatcherType.ERROR);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/api/does-not-exist");
        FilterChain errorPass = (req, res) -> seenInChain[1] = MDC.get(RequestIdFilter.MDC_KEY);
        filter.doFilter(request, response, errorPass);

        assertThat(seenInChain[0]).hasSize(36);
        assertThat(seenInChain[1]).isEqualTo(seenInChain[0]);
        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo(seenInChain[0]);
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }
}
