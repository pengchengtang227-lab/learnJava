package com.example.springstudy.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Spring Boot 生命周期事件监听器：观察框架发布的启动事件，与自定义事件对照学习。
 *
 * <p>这两个事件发生时容器已刷新，可以通过普通 Spring Bean 的 @EventListener 监听。
 * 更早的 ApplicationStartingEvent、ApplicationEnvironmentPreparedEvent 发生时，
 * 这些监听器 Bean 尚未准备好，需要通过 SpringApplication.addListeners 等方式提前注册。
 */
@Component
public class ApplicationLifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationLifecycleListener.class);

    @EventListener
    public void onStarted(ApplicationStartedEvent event) {
        LOGGER.info("[事件示例][生命周期] ApplicationStartedEvent：容器刷新完成，尚未执行 Runner");
    }

    @EventListener
    public void onReady(ApplicationReadyEvent event) {
        // 这里只观察事件。需要在应用就绪前完成的启动任务，应放在 Runner 中。
        LOGGER.info("[事件示例][生命周期] ApplicationReadyEvent：Runner 已执行完，应用就绪");
    }
}
