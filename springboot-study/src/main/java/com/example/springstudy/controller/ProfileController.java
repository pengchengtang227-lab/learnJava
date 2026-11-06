package com.example.springstudy.controller;

import com.example.springstudy.common.ApiResponse;
import com.example.springstudy.interceptor.LoginInterceptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录拦截器的演示接口：/api/secure/** 下的接口必须带有效令牌才能访问。
 */
@RestController
@RequestMapping("/api/secure")
@Tag(name = "登录演示", description = "点击页面中的 Authorize，填入演示令牌 demo-token")
@SecurityRequirement(name = "demoBearer")
public class ProfileController {

    // 用户名由 LoginInterceptor 在 preHandle 中放入 request，Controller 不需要再解析令牌。
    @GetMapping("/profile")
    @Operation(summary = "查看当前登录用户")
    public ApiResponse<String> profile(
            // 用户名由拦截器写入请求，不需要在 Swagger 页面中填写。
            @Parameter(hidden = true) @RequestAttribute(LoginInterceptor.USER_ATTRIBUTE) String user) {
        return ApiResponse.success(user);
    }
}
