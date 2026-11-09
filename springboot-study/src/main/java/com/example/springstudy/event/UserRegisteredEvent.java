package com.example.springstudy.event;

import java.util.Objects;
import lombok.Getter;

/**
 * 用户注册事件：描述“发生了什么”，并携带监听器需要的数据。
 *
 * <p>使用 @EventListener 时，事件可以是普通 Java 对象，无需继承 ApplicationEvent。
 * 事件本身不需要加 @Component；需要由 Spring 管理的是发布者和监听器。
 * 字段使用 final，避免某个监听器修改事件后影响其他监听器。
 */
@Getter
public final class UserRegisteredEvent {

    private final Long userId;

    public UserRegisteredEvent(Long userId) {
        this.userId = Objects.requireNonNull(userId, "用户 ID 不能为空");
    }

    @Override
    public String toString() {
        return "UserRegisteredEvent{userId=" + userId + '}';
    }
}
