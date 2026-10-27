package com.example.springstudy.repository;

import com.example.springstudy.dto.TransferRequest;
import java.util.Objects;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/** 转账记录与账户更新使用同一个数据源，因此可以参与同一个本地事务。 */
@Repository
public class TransferRepository {

    private final JdbcClient jdbcClient;

    public TransferRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Long saveTransfer(TransferRequest request) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO transfer_record (from_account_id, to_account_id, amount_in_cents)
                VALUES (:fromId, :toId, :amount)
                """)
                .param("fromId", request.fromAccountId())
                .param("toId", request.toAccountId())
                .param("amount", request.amountInCents())
                .update(keyHolder, "id");
        // H2 生成自增主键，KeyHolder 将主键带回 Java。
        return Objects.requireNonNull(keyHolder.getKey(), "转账记录主键未生成").longValue();
    }
}
