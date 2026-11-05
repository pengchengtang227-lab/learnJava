package com.example.springstudy.service.impl;

import com.example.springstudy.event.UserRegisteredEvent;
import com.example.springstudy.service.UserRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 事件发布者：完成自己的工作后，告诉其他组件“用户注册成功了”。
 *
 * <p>本例用日志模拟注册，不实际保存用户，也不改变账户转账示例的数据。
 * 发布者只依赖事件发布接口，无需直接依赖邮件监听器，便于扩展其他监听逻辑。
 */
@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationServiceImpl.class);

    private final ApplicationEventPublisher eventPublisher;

    // 构造器注入 Spring 提供的事件发布器，无需手动 new；单构造器不必加 @Autowired。
    public UserRegistrationServiceImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void register(Long userId) {
        UserRegisteredEvent event = new UserRegisteredEvent(userId);
        LOGGER.info("[事件示例][注册服务] 模拟用户注册成功，准备发布事件，userId={}", userId);

        // 按事件类型找到监听器。本项目使用默认同步广播，监听器会在当前调用线程中执行。
        eventPublisher.publishEvent(event);

        // 对照日志：监听器的输出应出现在“准备发布事件”和“发布方法已返回”之间。
        LOGGER.info("[事件示例][注册服务] 发布方法已返回，同步监听器已经执行完毕，userId={}", userId);
    }
}
