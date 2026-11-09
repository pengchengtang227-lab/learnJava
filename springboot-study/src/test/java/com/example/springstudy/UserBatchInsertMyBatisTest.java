package com.example.springstudy;

import com.example.springstudy.dto.BatchSaveUserRequest;
import com.example.springstudy.dto.SaveUserRequest;
import com.example.springstudy.exception.BusinessException;
import com.example.springstudy.mapper.UserMapper;
import com.example.springstudy.model.UserDO;
import com.example.springstudy.service.UserService;
import com.example.springstudy.service.impl.UserServiceImpl;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
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
import org.springframework.dao.DuplicateKeyException;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 每个用例使用独立 H2 库，真实执行 foreach SQL，并检查跨组事务回滚。 */
class UserBatchInsertMyBatisTest {

    private AnnotationConfigApplicationContext context;
    private UserService userService;
    private JdbcTemplate jdbc;
    private InsertCounter counter;

    @BeforeEach
    void setUp() throws Exception {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        DataSource dataSource = context.getBean(DataSource.class);
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("SET MODE MySQL");
        String schema = new ClassPathResource("db/migration/V1__init_schema_and_demo_accounts.sql")
                .getContentAsString(StandardCharsets.UTF_8).replace("user_name(50)", "user_name");
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new EncodedResource(
                    new ByteArrayResource(schema.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        }
        // H2 不支持 V2 的 MySQL 多子句 ALTER；仅在测试库补充实际用户插入所需字段。
        jdbc.execute("ALTER TABLE study_user ADD COLUMN gender INT NOT NULL DEFAULT 0 CHECK (gender BETWEEN 0 AND 2)");
        userService = context.getBean(UserService.class);
        counter = context.getBean(InsertCounter.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @ParameterizedTest
    @CsvSource({"1, 1, 1", "500, 1, 500", "501, 2, 1", "1001, 3, 1"})
    void insertsEveryUserWithExpectedGroupSizes(int count, int statements, int lastGroupSize) {
        assertEquals(count, userService.saveUsers(requests(count)));
        assertEquals((long) count, countUsers());
        assertEquals(statements, counter.parameterCounts.size());
        assertEquals(lastGroupSize * 4, counter.parameterCounts.get(statements - 1));
        for (int index = 0; index < statements - 1; index++) {
            assertEquals(2_000, counter.parameterCounts.get(index));
        }
        UserDO first = userService.searchUsers("batch-user-0", null, null).get(0);
        UserDO last = userService.searchUsers("batch-user-" + (count - 1), null, null).get(0);
        assertEquals(0, first.getGender());
        assertEquals(20, last.getAge());
        assertEquals("batch@example.com", last.getEmail());
        assertNotNull(last.getGmtCreate());
        assertNotNull(last.getGmtModified());
    }

    @Test
    void trimsUserNamesAndKeepsExplicitGender() {
        assertEquals(1, userService.saveUsers(
                List.of(new SaveUserRequest("  alice  ", 21, "alice@example.com", 2))));
        UserDO user = userService.searchUsers("alice", null, null).get(0);
        assertEquals("alice", user.getUserName());
        assertEquals(2, user.getGender());
    }

    @Test
    void rejectsDuplicateNamesAcrossGroupsBeforeAnyInsert() {
        List<SaveUserRequest> users = requests(501);
        users.set(500, new SaveUserRequest("  batch-user-0  ", 20, "batch@example.com", null));
        BusinessException failure = assertThrows(BusinessException.class, () -> userService.saveUsers(users));
        assertEquals(HttpStatus.CONFLICT, failure.getStatus());
        assertEquals("DATA_CONFLICT", failure.getCode());
        assertTrue(counter.parameterCounts.isEmpty());
        assertEquals(0L, countUsers());
    }

    @Test
    void secondGroupConflictRollsBackTheFirstGroup() {
        userService.saveUser(new SaveUserRequest("batch-user-500", 30, "existing@example.com", 1));
        counter.parameterCounts.clear();
        assertThrows(DuplicateKeyException.class, () -> userService.saveUsers(requests(501)));
        assertEquals(List.of(2_000, 4), counter.parameterCounts);
        assertEquals(1L, countUsers());
        // 本用例只验证回滚后保留的原用户；直接按姓名查询，不依赖列表接口的分页响应结构。
        UserDO existing = userService.searchUsers("batch-user-500", null, null).getFirst();
        assertEquals("batch-user-500", existing.getUserName());
        assertEquals("existing@example.com", existing.getEmail());
    }

    @Test
    void rejectsNullEmptyOversizeAndNullElementLists() {
        List<List<SaveUserRequest>> invalidLists = new ArrayList<>();
        invalidLists.add(null);
        invalidLists.add(List.of());
        invalidLists.add(Collections.singletonList(null));
        invalidLists.add(Collections.nCopies(10_001, new SaveUserRequest("alice", 20, "alice@example.com", null)));
        for (List<SaveUserRequest> users : invalidLists) {
            BusinessException failure = assertThrows(BusinessException.class, () -> userService.saveUsers(users));
            assertEquals(HttpStatus.BAD_REQUEST, failure.getStatus());
            assertEquals("INVALID_REQUEST", failure.getCode());
        }
        assertTrue(counter.parameterCounts.isEmpty());
        assertEquals(0L, countUsers());
    }

    @Test
    void validatesContainerAndEachUserBeforeControllerCallsService() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(new BatchSaveUserRequest(requests(1))).isEmpty());
            List<BatchSaveUserRequest> invalidRequests = List.of(
                    new BatchSaveUserRequest(null),
                    new BatchSaveUserRequest(List.of()),
                    new BatchSaveUserRequest(Collections.singletonList(null)),
                    new BatchSaveUserRequest(Collections.nCopies(10_001, requests(1).get(0))),
                    new BatchSaveUserRequest(List.of(new SaveUserRequest(" ", 20, "valid@example.com", null))),
                    new BatchSaveUserRequest(List.of(new SaveUserRequest("alice", 151, "valid@example.com", null))),
                    new BatchSaveUserRequest(List.of(new SaveUserRequest("alice", 20, "invalid", null))),
                    new BatchSaveUserRequest(List.of(new SaveUserRequest("alice", 20, "valid@example.com", 3))));
            for (BatchSaveUserRequest request : invalidRequests) {
                assertFalse(validator.validate(request).isEmpty());
            }
        }
    }

    private long countUsers() {
        // 在 Service 事务之外读取真实数据库状态，确认提交与回滚，而非只检查返回值。
        return jdbc.queryForObject("SELECT COUNT(*) FROM study_user", Long.class);
    }

    private List<SaveUserRequest> requests(int count) {
        List<SaveUserRequest> users = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            users.add(new SaveUserRequest("batch-user-" + index, 20, "batch@example.com", null));
        }
        return users;
    }

    @Intercepts(@Signature(type = StatementHandler.class, method = "update", args = Statement.class))
    public static class InsertCounter implements Interceptor {

        private final List<Integer> parameterCounts = new ArrayList<>();

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            StatementHandler handler = (StatementHandler) invocation.getTarget();
            if (handler.getBoundSql().getSql().stripLeading().startsWith("INSERT INTO study_user")) {
                parameterCounts.add(handler.getBoundSql().getParameterMappings().size());
            }
            return invocation.proceed();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @MapperScan(basePackageClasses = UserMapper.class)
    static class TestConfig {

        @Bean(destroyMethod = "shutdown")
        EmbeddedDatabase dataSource() {
            return new EmbeddedDatabaseBuilder().generateUniqueName(true)
                    .setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        InsertCounter insertCounter() {
            return new InsertCounter();
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource dataSource, InsertCounter counter) throws Exception {
            SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
            factory.setDataSource(dataSource);
            factory.setTypeAliasesPackage("com.example.springstudy.model");
            factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
            factory.setPlugins(counter);
            return factory.getObject();
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        UserService userService(UserMapper userMapper) {
            return new UserServiceImpl(userMapper);
        }
    }
}
