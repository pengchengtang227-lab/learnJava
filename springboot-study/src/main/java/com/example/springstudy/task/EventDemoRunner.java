package com.example.springstudy.task;

import com.example.springstudy.service.UserRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 启动任务：每次启动应用时，自动触发一次用户注册事件示例。
 *
 * <p>容器刷新完成、ApplicationStartedEvent 发布后，Spring Boot 调用 Runner 的 run 方法。
 * 所有 Runner 正常执行结束后，才会发布 ApplicationReadyEvent。
 * 这是应用启动任务，不是定时任务，也不会在每次 HTTP 请求时执行。
 */
@Component
public class EventDemoRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDemoRunner.class);
    private static final long DEMO_USER_ID = 1001L;

    private final UserRegistrationService userRegistrationService;

    public EventDemoRunner(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @Override
    public void run(String... args) {
        LOGGER.info("[事件示例][Runner] 启动任务开始，触发一次模拟注册");

        // Runner 只负责触发，具体业务逻辑放在 Service；args 是原始命令行参数，本例无需使用。
        userRegistrationService.register(DEMO_USER_ID);

        LOGGER.info("[事件示例][Runner] 启动任务执行结束");
    }
}
