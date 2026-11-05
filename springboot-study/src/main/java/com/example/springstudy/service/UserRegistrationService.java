package com.example.springstudy.service;

/** 用户注册的事件学习接口；本例只模拟注册，不写入数据库。 */
public interface UserRegistrationService {

    /** 模拟注册成功，并发布用户注册事件。 */
    void register(Long userId);
}
