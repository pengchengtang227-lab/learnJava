package com.example.springstudy;

import com.example.springstudy.controller.UserController;
import com.example.springstudy.exception.BusinessException;
import com.example.springstudy.exception.GlobalExceptionHandler;
import com.example.springstudy.mapper.UserMapper;
import com.example.springstudy.service.UserService;
import com.example.springstudy.service.impl.UserServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageInterceptor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.Duration;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 使用真实 Boot 自动配置、PageHelper Starter、HTTP 接口和独立 H2 库验证分页。 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserPaginationIntegrationTest {

    private static final int USER_COUNT = 25;
    private static final int ID_INTERVAL = 10;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private ConfigurableApplicationContext context;
    private JdbcTemplate jdbc;
    private UserService userService;
    private UserMapper userMapper;
    private String baseUrl;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @BeforeAll
    void startApplication() throws Exception {
        // 读取实际 application.yml 的 MyBatis/PageHelper 配置；命令行覆盖数据源，确保不连接 MySQL。
        context = new SpringApplication(TestApplication.class).run(
                "--spring.main.banner-mode=off", "--server.address=127.0.0.1", "--server.port=0",
                "--spring.datasource.url=jdbc:h2:mem:user_pagination_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "--spring.datasource.username=sa", "--spring.datasource.password=",
                "--spring.datasource.driver-class-name=org.h2.Driver",
                "--spring.flyway.enabled=false", "--spring.sql.init.mode=never",
                "--logging.level.root=ERROR", "--logging.level.com.example.springstudy=ERROR");
        DataSource dataSource = context.getBean(DataSource.class);
        jdbc = new JdbcTemplate(dataSource);
        String schema = new ClassPathResource("db/migration/V1__init_schema_and_demo_accounts.sql")
                .getContentAsString(StandardCharsets.UTF_8).replace("user_name(50)", "user_name");
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new EncodedResource(
                    new ByteArrayResource(schema.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        }
        jdbc.execute("ALTER TABLE study_user ADD COLUMN gender INT NOT NULL DEFAULT 0 CHECK (gender BETWEEN 0 AND 2)");
        userService = context.getBean(UserService.class);
        userMapper = context.getBean(UserMapper.class);
        baseUrl = "http://127.0.0.1:" + context.getEnvironment().getProperty("local.server.port");
    }

    @BeforeEach
    void prepareUsers() {
        jdbc.update("DELETE FROM study_user");
        // ID 有间隔，验证插件按记录位置分页，不会误把页码换算成主键区间。
        for (int index = 1; index <= USER_COUNT; index++) {
            jdbc.update("INSERT INTO study_user (id, user_name, age, email, gender) VALUES (?, ?, ?, ?, ?)",
                    index * ID_INTERVAL, "page-user-" + index, 20, "page@example.com", 0);
        }
    }

    @AfterAll
    void closeApplication() {
        if (Objects.nonNull(context)) {
            context.close();
        }
    }

    @Test
    void starterRegistersExactlyOnePaginationInterceptor() {
        long interceptors = context.getBean(SqlSessionFactory.class).getConfiguration().getInterceptors().stream()
                .filter(PageInterceptor.class::isInstance).count();
        assertEquals(1L, interceptors);
    }

    @Test
    void omittedParametersReturnFirstPageWithMetadata() throws Exception {
        JsonNode page = get("/api/users", 200).get("data");
        assertEquals(1, page.get("pageNum").asInt());
        assertEquals(10, page.get("pageSize").asInt());
        assertEquals(25L, page.get("total").asLong());
        assertEquals(3, page.get("pages").asInt());
        assertEquals(10, page.get("list").size());
        assertEquals(10L, page.get("list").get(0).get("id").asLong());
        assertTrue(page.get("hasNextPage").asBoolean());
        assertFalse(page.get("hasPreviousPage").asBoolean());
    }

    @ParameterizedTest
    @CsvSource({"2, 10, 10, 110, 200", "3, 10, 5, 210, 250", "2, 7, 7, 80, 140", "1, 100, 25, 10, 250"})
    void returnsRequestedPageInStableOrder(int pageNum, int pageSize, int count, long firstId, long lastId)
            throws Exception {
        JsonNode page = get("/api/users?pageNum=" + pageNum + "&pageSize=" + pageSize, 200).get("data");
        assertEquals(pageNum, page.get("pageNum").asInt());
        assertEquals(pageSize, page.get("pageSize").asInt());
        assertEquals(USER_COUNT, page.get("total").asInt());
        assertEquals((USER_COUNT + pageSize - 1) / pageSize, page.get("pages").asInt());
        assertEquals(count, page.get("list").size());
        assertEquals(firstId, page.get("list").get(0).get("id").asLong());
        assertEquals(lastId, page.get("list").get(count - 1).get("id").asLong());
    }

    @Test
    void pagePastTheEndReturnsEmptyListAndKeepsTotal() throws Exception {
        JsonNode page = get("/api/users?pageNum=4&pageSize=10", 200).get("data");
        assertEquals(4, page.get("pageNum").asInt());
        assertEquals(25L, page.get("total").asLong());
        assertEquals(3, page.get("pages").asInt());
        assertEquals(0, page.get("list").size());
    }

    @Test
    void emptyTableReturnsZeroTotalAndEmptyList() throws Exception {
        jdbc.update("DELETE FROM study_user");
        JsonNode page = get("/api/users", 200).get("data");
        assertEquals(0L, page.get("total").asLong());
        assertEquals(0, page.get("pages").asInt());
        assertEquals(0, page.get("list").size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"pageNum=0", "pageNum=-1", "pageSize=0", "pageSize=-1", "pageSize=101",
            "pageNum=abc", "pageSize=abc", "pageNum=2147483648", "pageNum=2147483647&pageSize=100"})
    void rejectsInvalidPaginationParameters(String query) throws Exception {
        JsonNode failure = get("/api/users?" + query, 400);
        assertEquals("INVALID_REQUEST", failure.get("code").asString());
        assertTrue(failure.get("data").isNull());
    }

    @Test
    void serviceValidatesParametersAndLeavesNoPaginationState() {
        BusinessException failure = assertThrows(BusinessException.class, () -> userService.listUsers(1, 0));
        assertEquals(HttpStatus.BAD_REQUEST, failure.getStatus());
        assertNull(PageHelper.getLocalPage());
    }

    @Test
    void paginationDoesNotAffectFollowingMapperQuery() {
        PageInfo<?> page = userService.listUsers(2, 10);
        assertEquals(25L, page.getTotal());
        assertEquals(10, page.getList().size());
        assertNull(PageHelper.getLocalPage());
        assertEquals(USER_COUNT, userMapper.listUsers().size());
    }

    @Test
    void failedQueryAlsoClearsPaginationState() {
        jdbc.execute("ALTER TABLE study_user RENAME TO unavailable_user");
        try {
            assertThrows(BadSqlGrammarException.class, () -> userService.listUsers(2, 10));
        } finally {
            jdbc.execute("ALTER TABLE unavailable_user RENAME TO study_user");
        }
        assertNull(PageHelper.getLocalPage());
        assertEquals(USER_COUNT, userMapper.listUsers().size());
    }

    @Test
    void swaggerDocumentsPaginationParametersAndResponse() throws Exception {
        JsonNode operation = get("/v3/api-docs", 200).get("paths").get("/api/users").get("get");
        String parameters = operation.get("parameters").toString();
        assertTrue(parameters.contains("pageNum"));
        assertTrue(parameters.contains("pageSize"));
        assertFalse(operation.get("responses").get("200").isNull());
    }

    private JsonNode get(String path, int expectedStatus) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json").GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(expectedStatus, response.statusCode(), response.body());
        JsonNode body = JSON.readTree(response.body());
        if (path.startsWith("/api/") && expectedStatus == 200) {
            assertEquals("SUCCESS", body.get("code").asString());
        }
        return body;
    }

    /** 显式导入待测业务组件，避免扫描到其他测试配置，同时验证 Starter 的真实自动配置。 */
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @MapperScan(basePackageClasses = UserMapper.class)
    @Import({UserController.class, UserServiceImpl.class, GlobalExceptionHandler.class})
    static class TestApplication { }
}
