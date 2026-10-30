package com.example.springstudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动入口。@SpringBootApplication 开启自动配置，并扫描当前包及子包中的 Spring 组件。
 * Controller、Service、Repository 和日志切面都放在此包下面，因此能被自动发现。
 *
 * <p>Spring MVC 的核心是 DispatcherServlet，它是所有请求的统一入口。请求进来后，它先通过 HandlerMapping 根据 URL 找到对应的 Controller 方法和拦截器，组成执行链；
 * 然后交给 HandlerAdapter，由它完成参数绑定并调用 Controller 方法。
 *
 * <p>如果方法返回的是视图名，ViewResolver 会把它解析成真实的视图，再用 Model 数据渲染成页面返回。现在前后端分离的项目一般用 @RestController，
 * 返回值由 HttpMessageConverter 直接转成 JSON 写入响应，不再经过视图解析和渲染。
 */
@SpringBootApplication
public class SpringStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringStudyApplication.class, args);
    }
}
