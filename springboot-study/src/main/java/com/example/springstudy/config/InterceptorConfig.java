package com.example.springstudy.config;

import com.example.springstudy.interceptor.AccessLogInterceptor;
import com.example.springstudy.interceptor.LoginInterceptor;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 声明由 Spring 管理的拦截器。实例创建与 WebMvcConfig 中的路径注册分开。
 */
@Configuration
public class InterceptorConfig {

    @Bean
    public AccessLogInterceptor accessLogInterceptor() {
        return new AccessLogInterceptor();
    }

    @Bean
    public LoginInterceptor loginInterceptor() {
        // 学习用的固定令牌表；真实项目应使用经过校验的 JWT 或服务端会话。
        return new LoginInterceptor(Map.of("demo-token", "alice"));
    }
}
