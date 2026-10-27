package com.example.springstudy.repository;

import com.example.springstudy.model.AccountDO;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * 数据访问层只负责 SQL。JdbcClient 是 Spring 提供的 JDBC 门面，简化连接和参数绑定。
 * @Repository 将该类注册为 Bean；SQL 中使用命名参数，避免拼接用户输入。
 */
@Repository
public class AccountRepository {

    private static final RowMapper<AccountDO> ACCOUNT_ROW_MAPPER = (resultSet, rowNumber) ->
            new AccountDO(resultSet.getLong("id"), resultSet.getString("owner"),
                    resultSet.getLong("balance_in_cents"));

    private final JdbcClient jdbcClient;

    public AccountRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<AccountDO> listAccounts() {
        return jdbcClient.sql("SELECT id, owner, balance_in_cents FROM account ORDER BY id")
                .query(ACCOUNT_ROW_MAPPER)
                .list();
    }

    public Optional<AccountDO> findById(Long accountId) {
        return jdbcClient.sql("SELECT id, owner, balance_in_cents FROM account WHERE id = :id")
                .param("id", accountId)
                .query(ACCOUNT_ROW_MAPPER)
                .optional();
    }

    public boolean deductBalance(Long accountId, Long amountInCents) {
        // 扣款与余额条件放在同一条 SQL 中，避免“先查余额再扣款”造成并发超扣。
        return jdbcClient.sql("""
                UPDATE account
                SET balance_in_cents = balance_in_cents - :amount
                WHERE id = :id AND balance_in_cents >= :amount
                """)
                .param("amount", amountInCents)
                .param("id", accountId)
                .update() > 0;
    }

    public boolean addBalance(Long accountId, Long amountInCents) {
        return jdbcClient.sql("""
                UPDATE account SET balance_in_cents = balance_in_cents + :amount WHERE id = :id
                """)
                .param("amount", amountInCents)
                .param("id", accountId)
                .update() > 0;
    }
}
