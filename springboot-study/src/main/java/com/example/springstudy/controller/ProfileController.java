package com.example.springstudy.controller;

import com.example.springstudy.common.ApiResponse;
import com.example.springstudy.interceptor.LoginInterceptor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录拦截器的演示接口：/api/secure/** 下的接口必须带有效令牌才能访问。
 */
@RestController
@RequestMapping("/api/secure")
public class ProfileController {

    // 用户名由 LoginInterceptor 在 preHandle 中放入 request，Controller 不需要再解析令牌。
    @GetMapping("/profile")
    public ApiResponse<String> profile(@RequestAttribute(LoginInterceptor.USER_ATTRIBUTE) String user) {
        return ApiResponse.success(user);
    }
}
