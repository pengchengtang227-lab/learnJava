package com.example.springstudy.exception;

import com.example.springstudy.common.ApiResponse;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 集中处理 MVC 请求过程中产生的异常，Controller 和 Service 不必反复写 try/catch。
 * 异常先经过事务代理（完成回滚），再到达这里，最后转换成 HTTP 状态码和 JSON。
 * 继承框架的异常处理器，可以保留 404、405、415 等正确状态及 Allow 等响应头。
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INVALID_REQUEST_CODE = "INVALID_REQUEST";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        // 可预期失败用 WARN，不向客户端返回 Java 堆栈。
        LOGGER.warn("业务请求未完成：code={}，message={}", exception.getCode(), exception.getMessage());
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.failure(exception.getCode(), exception.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        // 只返回字段名和约束提示，不回显用户提交的原始值。
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining("；"));
        return new ResponseEntity<>(ApiResponse.failure(INVALID_REQUEST_CODE, message), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        // 参数类型错误、JSON 格式错误、路径参数校验失败等由父类分派到这里。
        // 保留框架给出的状态码，不把客户端的 4xx 错误误报成服务器 500。
        String message = status.value() == HttpStatus.BAD_REQUEST.value()
                ? "请求参数不合法，请检查参数类型、范围和 JSON 格式"
                : "请求无法处理，请检查路径、HTTP 方法和 Content-Type";
        return new ResponseEntity<>(ApiResponse.failure(INVALID_REQUEST_CODE, message), headers, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        // 兜底只放在请求边界：服务端记录完整堆栈，客户端拿到稳定且不泄露内部细节的提示。
        LOGGER.error("处理请求时出现未预期异常", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("INTERNAL_ERROR", "服务器内部错误，请稍后重试"));
    }
}
