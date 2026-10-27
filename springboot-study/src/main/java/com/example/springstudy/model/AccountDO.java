package com.example.springstudy.model;

/**
 * 账户数据，对应 account 表。
 * 金额以“分”为单位保存为 Long，例如 100000 表示 1000 元，避免浮点数精度问题。
 * 此示例没有敏感字段，因此直接作为查询接口的数据；复杂项目通常另建响应 DTO。
 */
public record AccountDO(Long id, String owner, Long balanceInCents) {
}
