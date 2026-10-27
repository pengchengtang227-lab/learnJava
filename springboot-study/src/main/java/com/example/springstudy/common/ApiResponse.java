package com.example.springstudy.common;

/**
 * 统一响应结构。HTTP 状态码表达请求结果，code 表达具体业务结果，两者各有用途。
 * record 是 Java 的不可变数据载体，会自动生成构造器、访问方法、equals 和 toString。
 *
 * @param code 机器可读的业务码，例如 SUCCESS、ACCOUNT_NOT_FOUND
 * @param message 面向调用者的提示
 * @param data 成功时的业务数据；失败时为 null
 */
public record ApiResponse<T>(String code, String message, T data) {

    private static final String SUCCESS_CODE = "SUCCESS";

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_CODE, "操作成功", data);
    }

    public static ApiResponse<Void> failure(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
