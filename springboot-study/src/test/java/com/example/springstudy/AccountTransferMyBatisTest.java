package com.example.springstudy;

import com.example.springstudy.dto.TransferRequest;
import com.example.springstudy.dto.TransferVO;
import com.example.springstudy.exception.BusinessException;
import com.example.springstudy.mapper.AccountMapper;
import com.example.springstudy.mapper.TransferMapper;
import com.example.springstudy.model.AccountDO;
import com.example.springstudy.service.AccountService;
import com.example.springstudy.service.impl.AccountServiceImpl;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 使用真实 Mapper 和 Spring 事务代理验证迁移行为；每个用例都有独立的内存数据库。 */
class AccountTransferMyBatisTest {

    private AnnotationConfigApplicationContext context;
    private AccountService accountService;
    private AccountMapper accountMapper;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() throws Exception {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        DataSource dataSource = context.getBean(DataSource.class);
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("SET MODE MySQL");
        // 使用实际 V1 建表和初始账户；H2 不支持 MySQL 的字符串前缀索引。
        String schema = new ClassPathResource("db/migration/V1__init_schema_and_demo_accounts.sql")
                .getContentAsString(StandardCharsets.UTF_8).replace("user_name(50)", "user_name");
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new EncodedResource(
                    new ByteArrayResource(schema.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        }
        accountService = context.getBean(AccountService.class);
        accountMapper = context.getBean(AccountMapper.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void mapsAccountRecordsAndSortsById() {
        assertEquals(List.of(new AccountDO(1L, "小明", 100_000L),
                new AccountDO(2L, "小红", 50_000L)), accountService.listAccounts());
        assertEquals(new AccountDO(1L, "小明", 100_000L), accountService.getAccount(1L));
    }

    @Test
    void missingAccountReturnsNullFromMapperAndNotFoundFromService() {
        assertNull(accountMapper.getAccountById(999L));
        BusinessException failure = assertThrows(BusinessException.class, () -> accountService.getAccount(999L));
        assertEquals(HttpStatus.NOT_FOUND, failure.getStatus());
        assertEquals("ACCOUNT_NOT_FOUND", failure.getCode());
    }

    @Test
    void commitsBalancesAndReturnsGeneratedTransferId() {
        TransferVO result = accountService.transfer(new TransferRequest(1L, 2L, 1_000L, false));

        assertNotNull(result.transferId());
        assertTrue(result.transferId() > 0);
        assertEquals(new TransferVO(result.transferId(), 1L, 2L, 1_000L), result);
        assertDatabaseState(99_000L, 51_000L, 1L);
        assertEquals(result.transferId(), jdbc.queryForObject("SELECT id FROM transfer_record", Long.class));
        assertEquals(1L, jdbc.queryForObject("SELECT from_account_id FROM transfer_record", Long.class));
        assertEquals(2L, jdbc.queryForObject("SELECT to_account_id FROM transfer_record", Long.class));
        assertEquals(1_000L, jdbc.queryForObject("SELECT amount_in_cents FROM transfer_record", Long.class));
    }

    @Test
    void allowsTransferringTheEntireBalance() {
        accountService.transfer(new TransferRequest(1L, 2L, 100_000L, false));
        assertDatabaseState(0L, 150_000L, 1L);
    }

    @Test
    void conditionalUpdatePreventsOverdraftWithoutServicePrecheck() {
        assertEquals(0, accountMapper.deductBalance(1L, 100_001L));
        assertEquals(0, accountMapper.deductBalance(999L, 1L));
        assertEquals(0, accountMapper.addBalance(999L, 1L));
        assertDatabaseState(100_000L, 50_000L, 0L);
    }

    @Test
    void insufficientFundsLeaveBalancesAndRecordsUnchanged() {
        BusinessException failure = assertThrows(BusinessException.class,
                () -> accountService.transfer(new TransferRequest(1L, 2L, 100_001L, false)));
        assertEquals(HttpStatus.CONFLICT, failure.getStatus());
        assertEquals("INSUFFICIENT_BALANCE", failure.getCode());
        assertDatabaseState(100_000L, 50_000L, 0L);
    }

    @ParameterizedTest
    @CsvSource({"999, 2", "1, 999"})
    void missingTransferAccountLeavesDatabaseUnchanged(long fromId, long toId) {
        BusinessException failure = assertThrows(BusinessException.class,
                () -> accountService.transfer(new TransferRequest(fromId, toId, 1_000L, false)));
        assertEquals(HttpStatus.NOT_FOUND, failure.getStatus());
        assertEquals("ACCOUNT_NOT_FOUND", failure.getCode());
        assertDatabaseState(100_000L, 50_000L, 0L);
    }

    @Test
    void rejectsTransfersToTheSameAccount() {
        BusinessException failure = assertThrows(BusinessException.class,
                () -> accountService.transfer(new TransferRequest(1L, 1L, 1_000L, false)));
        assertEquals(HttpStatus.BAD_REQUEST, failure.getStatus());
        assertEquals("SAME_ACCOUNT", failure.getCode());
        assertDatabaseState(100_000L, 50_000L, 0L);
    }

    @Test
    void simulatedFailureRollsBackBothBalanceUpdatesAndInsertedRecord() {
        BusinessException failure = assertThrows(BusinessException.class,
                () -> accountService.transfer(new TransferRequest(1L, 2L, 1_000L, true)));
        assertEquals(HttpStatus.CONFLICT, failure.getStatus());
        assertEquals("DEMO_ROLLBACK", failure.getCode());
        assertDatabaseState(100_000L, 50_000L, 0L);
    }

    @Test
    void insertFailureRollsBackPreviouslyUpdatedBalances() {
        // 只在本用例的内存库中制造第三次写入失败，验证前两次写入确实参加同一事务。
        jdbc.execute("ALTER TABLE transfer_record ADD CONSTRAINT test_amount_limit CHECK (amount_in_cents < 1000)");
        assertThrows(DataIntegrityViolationException.class,
                () -> accountService.transfer(new TransferRequest(1L, 2L, 1_000L, false)));
        assertDatabaseState(100_000L, 50_000L, 0L);
    }

    private void assertDatabaseState(long fromBalance, long toBalance, long recordCount) {
        // 独立查询数据库，避免事务内 MyBatis 一级缓存掩盖未提交或未回滚的问题。
        assertEquals(fromBalance, jdbc.queryForObject(
                "SELECT balance_in_cents FROM account WHERE id = 1", Long.class));
        assertEquals(toBalance, jdbc.queryForObject(
                "SELECT balance_in_cents FROM account WHERE id = 2", Long.class));
        assertEquals(recordCount, jdbc.queryForObject("SELECT COUNT(*) FROM transfer_record", Long.class));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @MapperScan(basePackageClasses = AccountMapper.class)
    static class TestConfig {

        @Bean(destroyMethod = "shutdown")
        EmbeddedDatabase dataSource() {
            return new EmbeddedDatabaseBuilder().generateUniqueName(true)
                    .setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
            SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
            factory.setDataSource(dataSource);
            factory.setTypeAliasesPackage("com.example.springstudy.model");
            factory.setMapperLocations(new PathMatchingResourcePatternResolver()
                    .getResources("classpath:mapper/*.xml"));
            return factory.getObject();
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        AccountService accountService(AccountMapper accountMapper, TransferMapper transferMapper) {
            return new AccountServiceImpl(accountMapper, transferMapper);
        }
    }
}
