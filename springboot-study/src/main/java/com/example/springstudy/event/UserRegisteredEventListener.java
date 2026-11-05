package com.example.springstudy.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 自定义事件监听器：只关心“注册后做什么”，不负责执行注册。
 *
 * <p>@Component 使监听器被组件扫描发现；@EventListener 按方法参数类型匹配事件。
 * 以后还可以添加其他监听器处理积分、优惠券等逻辑，发布者无需逐个调用它们。
 */
@Component
public class UserRegisteredEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegisteredEventListener.class);

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        // 这里只打印日志，不连接邮件服务器。真实邮件发送可以交给独立的邮件 Service。
        LOGGER.info("[事件示例][用户监听器] 收到注册事件，模拟发送欢迎邮件，userId={}", event.getUserId());
    }
}
