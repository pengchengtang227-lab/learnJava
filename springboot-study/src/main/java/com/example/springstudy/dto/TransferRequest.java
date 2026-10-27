package com.example.springstudy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 接收转账 JSON。Controller 的 @Valid 会触发这些约束；校验失败时不会执行转账。
 * Boot 4 使用 jakarta.validation 包，旧教程中的 javax.validation 不适用。
 *
 * @param fromAccountId 转出账户
 * @param toAccountId 转入账户
 * @param amountInCents 转账金额，单位：分
 * @param simulateFailure 学习用开关：为 true 时在数据库写入后主动抛异常，观察整体回滚
 */
public record TransferRequest(
        @NotNull(message = "转出账户不能为空")
        @Positive(message = "转出账户 ID 必须大于 0")
        Long fromAccountId,

        @NotNull(message = "转入账户不能为空")
        @Positive(message = "转入账户 ID 必须大于 0")
        Long toAccountId,

        @NotNull(message = "转账金额不能为空")
        @Positive(message = "转账金额必须大于 0 分")
        @Max(value = 100_000_000L, message = "单次转账金额不能超过 100000000 分")
        Long amountInCents,

        Boolean simulateFailure) {
}
