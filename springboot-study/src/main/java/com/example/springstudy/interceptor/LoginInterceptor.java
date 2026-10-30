package com.example.springstudy.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：在 Controller 执行前检查请求头 Authorization: Bearer &lt;token&gt;。
 * 没有令牌或令牌无效时，直接返回 401 并终止请求，Controller 不会被调用。
 * 学习用途：令牌只和固定的“令牌 -> 用户名”表比较。真实项目应校验 JWT 签名与过期时间，或到 Redis / 数据库查询会话。
 * 和 AccessLogInterceptor 一样不是 @Component，由 WebMvcConfig 注册并决定生效路径。
 */
public class LoginInterceptor implements HandlerInterceptor {

    /** 登录通过后放进 request 的用户名，Controller 可以用 @RequestAttribute 读取。 */
    // 注解属性要求编译期常量，所以这里用字符串字面量，不能用 Class.getName() 拼接。
    public static final String USER_ATTRIBUTE = "loginUser";

    private static final String BEARER_PREFIX = "Bearer ";
    // 格式与 ApiResponse 保持一致（code / message / data）。
    private static final String UNAUTHORIZED_BODY =
            "{\"code\":\"UNAUTHORIZED\",\"message\":\"未登录或令牌无效\",\"data\":null}";

    private final Map<String, String> tokenToUser;

    public LoginInterceptor(Map<String, String> tokenToUser) {
        this.tokenToUser = tokenToUser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // 浏览器跨域时会先发不带令牌的 OPTIONS 预检请求，必须放行，否则跨域调用永远失败。
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String user = resolveUser(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (user == null) {
            // 拦截器在 Controller 之外，抛出的异常不会走 GlobalExceptionHandler，所以这里直接写响应。
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(UNAUTHORIZED_BODY);
            return false;
        }
        request.setAttribute(USER_ATTRIBUTE, user);
        return true;
    }

    private String resolveUser(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return tokenToUser.get(authorization.substring(BEARER_PREFIX.length()).trim());
    }
}
