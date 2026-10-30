package com.example.springstudy.config;

import com.example.springstudy.interceptor.AccessLogInterceptor;
import com.example.springstudy.interceptor.LoginInterceptor;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器。过滤器靠 @Component 自动注册，拦截器则必须在这里手动加入，并指定生效的路径。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AccessLogInterceptor())
                // 只拦截业务接口；多个拦截器的 preHandle 按注册顺序执行，afterCompletion 按相反顺序执行。
                .addPathPatterns("/api/**");

        // 登录校验只作用于 /api/secure/**，其余学习用的接口保持匿名可访问。
        // 演示令牌写死在代码里；真实项目的令牌来自登录接口签发的 JWT 或服务端会话。
        registry.addInterceptor(new LoginInterceptor(Map.of("demo-token", "alice")))
                .addPathPatterns("/api/secure/**");
    }
}
