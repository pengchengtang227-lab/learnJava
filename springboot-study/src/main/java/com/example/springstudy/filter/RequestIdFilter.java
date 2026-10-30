package com.example.springstudy.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 过滤器（Servlet 层）：为每个请求分配一个请求 ID，写入日志上下文和响应头。
 * 过滤器在 DispatcherServlet 之前执行，覆盖所有请求，包括 404 和静态资源，所以适合做这类“全局、与业务无关”的事。
 * 继承 OncePerRequestFilter 可保证同一类分派只执行一次（内部转发不会重复进入）；错误分派默认被跳过，这里显式放开，见 shouldNotFilterErrorDispatch。
 * 标注 @Component 后，Spring Boot 会自动把它注册为 Servlet 过滤器，默认拦截所有路径。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    // 只接受简单的 ID：客户端传来的值会写进日志和响应头，不做限制可能造成日志伪造。
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");

    // 同一个请求的请求 ID 存在 request 上，错误分派再次进入过滤器时复用，避免生成第二个 ID。
    private static final String REQUEST_ID_ATTRIBUTE = RequestIdFilter.class.getName() + ".requestId";

    /**
     * OncePerRequestFilter 默认不处理 ERROR 分派。404 或未捕获的异常会触发容器转发到 /error，
     * 此时第一轮过滤器已经退出、MDC 已清理，错误页的日志没有请求 ID，容器重置响应时还可能丢掉响应头。
     * 返回 false 让错误分派也经过本过滤器。
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
        if (requestId == null) {
            requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
            request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        }
        // MDC 绑定在当前线程上，之后同一线程里所有日志都能通过 %X{requestId} 带上它。
        MDC.put(MDC_KEY, requestId);
        // 先写响应头：响应一旦提交，再设置就无效了。
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            // 放行，交给后面的过滤器、DispatcherServlet、拦截器和 Controller。
            chain.doFilter(request, response);
        } finally {
            // 容器线程会被复用，必须清理，否则下一个请求会看到上一个请求的 ID。
            MDC.remove(MDC_KEY);
        }
    }

    private String resolveRequestId(String incoming) {
        if (incoming != null && SAFE_REQUEST_ID.matcher(incoming).matches()) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }
}
