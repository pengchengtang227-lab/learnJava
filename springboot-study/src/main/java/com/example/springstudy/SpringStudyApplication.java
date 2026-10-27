package com.example.springstudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动入口。@SpringBootApplication 开启自动配置，并扫描当前包及子包中的 Spring 组件。
 * Controller、Service、Repository 和日志切面都放在此包下面，因此能被自动发现。
 */
@SpringBootApplication
public class SpringStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringStudyApplication.class, args);
    }
}
