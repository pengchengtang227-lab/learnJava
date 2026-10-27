package com.example.springstudy.dto;

/** 转账成功后的响应。transferId 对应本次在 transfer_record 表中保存的记录。 */
public record TransferVO(Long transferId, Long fromAccountId, Long toAccountId, Long amountInCents) {
}
