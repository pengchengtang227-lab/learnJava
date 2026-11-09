package com.example.springstudy.model;

import lombok.Getter;
import lombok.Setter;

/** 对应 transfer_record 表；使用可变 JavaBean，供 MyBatis 回填数据库自增主键。 */
@Getter
@Setter
public class TransferRecordDO {

    private Long id;
    private Long fromAccountId;
    private Long toAccountId;
    private Long amountInCents;

    @Override
    public String toString() {
        return "TransferRecordDO{id=" + id + "}";
    }
}
