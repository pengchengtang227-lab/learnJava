package com.example.springstudy.exception;

import org.springframework.http.HttpStatus;

/**
 * 可预期的业务异常，例如账户不存在、余额不足。
 * 继承 RuntimeException：默认就会触发 Spring 事务回滚。
 */
public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
