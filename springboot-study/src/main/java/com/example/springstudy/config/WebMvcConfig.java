package com.example.springstudy.config;

import com.example.springstudy.interceptor.AccessLogInterceptor;
import com.example.springstudy.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器及其生效路径。拦截器声明为 Spring Bean 后，仍需加入 MVC 的执行链。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AccessLogInterceptor accessLogInterceptor;
    private final LoginInterceptor loginInterceptor;

    public WebMvcConfig(AccessLogInterceptor accessLogInterceptor, LoginInterceptor loginInterceptor) {
        this.accessLogInterceptor = accessLogInterceptor;
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLogInterceptor)
                .addPathPatterns("/api/**")
                // 日志先进入、后退出，登录校验拒绝的请求也能记录状态码和耗时。
                .order(0);

        // 登录校验只作用于 /api/secure/**，其余学习用的接口保持匿名可访问。
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/secure/**")
                .order(1);
    }
}
