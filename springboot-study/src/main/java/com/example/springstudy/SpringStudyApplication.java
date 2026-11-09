package com.example.springstudy;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动入口。@SpringBootApplication 开启自动配置，并扫描当前包及子包中的 Spring 组件。
 * Controller、Service 和日志切面由组件扫描发现；子包中的 @Mapper 接口由 MyBatis Starter 扫描。
 * @EnableScheduling 开启定时任务，Spring 会自动处理任务 Bean 中的 @Scheduled 方法。
 *
 * <p>Spring MVC 的核心是 DispatcherServlet，它是所有请求的统一入口。请求进来后，它先通过 HandlerMapping 根据 URL 找到对应的 Controller 方法和拦截器，组成执行链；
 * 然后交给 HandlerAdapter，由它完成参数绑定并调用 Controller 方法。
 *
 * <p>如果方法返回的是视图名，ViewResolver 会把它解析成真实的视图，再用 Model 数据渲染成页面返回。现在前后端分离的项目一般用 @RestController，
 * 返回值由 HttpMessageConverter 直接转成 JSON 写入响应，不再经过视图解析和渲染。
 */
@SpringBootApplication
@EnableScheduling
// 文档标题和认证方式直接声明在启动类上，无需再增加一个配置类。
@OpenAPIDefinition(info = @Info(title = "Spring Boot 学习项目接口", version = "1.0.0",
        description = "用户增删改查、账户转账和登录拦截器演示。展开接口、填写参数后点击 Execute。"))
@SecurityScheme(name = "demoBearer", type = SecuritySchemeType.HTTP, scheme = "bearer",
        description = "仅登录演示接口需要。点击 Authorize，填入 demo-token，无需手动添加 Bearer 前缀。")
public class SpringStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringStudyApplication.class, args);
    }
}
