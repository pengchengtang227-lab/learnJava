package com.example.springstudy.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 拦截器（Spring MVC 层）：记录一次接口访问的“HTTP 方法、路径、处理方法、状态码、耗时”。
 * 拦截器能拿到 handler（具体处理请求的 Controller 方法），这是过滤器做不到的。
 * 和 ApiLogAspect 的分工：切面只能看到 Controller 方法的调用，拿不到 HTTP 状态码，
 * 而且参数校验失败、404 等请求根本不会进入 Controller 方法；拦截器的 afterCompletion 对这些请求也会执行。
 * 它不是 @Component，由 WebMvcConfig 注册，并决定拦截哪些路径。
 */
public class AccessLogInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogInterceptor.class);
    private static final String START_NANOS_ATTRIBUTE = AccessLogInterceptor.class.getName() + ".startNanos";

    /** Controller 方法执行之前。返回 true 放行；返回 false 则请求在此终止，后面的拦截器和 Controller 都不会执行。 */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 拦截器是单例，不能把开始时间存成成员变量；存在 request 上，每个请求各自一份。
        request.setAttribute(START_NANOS_ATTRIBUTE, System.nanoTime());
        return true;
    }

    /** 整个请求处理完之后执行，无论正常还是异常。这里已经能拿到最终的 HTTP 状态码。 */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception exception) {
        Object start = request.getAttribute(START_NANOS_ATTRIBUTE);
        long elapsedMillis = start instanceof Long startNanos
                ? TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)
                : -1;
        // 只记录路径，不记录查询字符串和请求体，避免把令牌等敏感信息写进日志。
        LOGGER.info("接口访问：{} {}，处理者={}，状态码={}，耗时={}ms",
                request.getMethod(), request.getRequestURI(), describe(handler), response.getStatus(), elapsedMillis);
    }

    private String describe(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            return handlerMethod.getShortLogMessage();
        }
        // 找不到 Controller 方法时（例如 404），handler 可能是静态资源处理器等其它类型。
        return handler == null ? "无" : handler.getClass().getSimpleName();
    }
}
