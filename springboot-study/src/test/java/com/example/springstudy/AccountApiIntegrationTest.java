package com.example.springstudy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 集成测试启动真实 Spring 容器和 H2，通过 MockMvc 走完整的 MVC 调用链。
 * 不在测试类上加 @Transactional：必须让 Service 自己提交/回滚，才能真正验证事务边界。
 * 每次测试重新准备数据，不依赖测试顺序，也不连接外部数据库。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class AccountApiIntegrationTest {

    private static final long FROM_ACCOUNT_ID = 1L;
    private static final long TO_ACCOUNT_ID = 2L;
    private static final long INITIAL_FROM_BALANCE = 100_000L;
    private static final long INITIAL_TO_BALANCE = 50_000L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void resetDatabase() {
        jdbcClient.sql("DELETE FROM transfer_record").update();
        jdbcClient.sql("UPDATE account SET balance_in_cents = :balance WHERE id = :id")
                .param("balance", INITIAL_FROM_BALANCE).param("id", FROM_ACCOUNT_ID).update();
        jdbcClient.sql("UPDATE account SET balance_in_cents = :balance WHERE id = :id")
                .param("balance", INITIAL_TO_BALANCE).param("id", TO_ACCOUNT_ID).update();
    }

    @Test
    void listAccountsReturnsDataAndWritesAspectLogs(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].owner").value("小明"))
                .andExpect(jsonPath("$.data[0].balanceInCents").value(INITIAL_FROM_BALANCE));

        assertThat(output.getOut()).contains("接口开始", "AccountController.listAccounts()", "正常返回=true", "耗时=");
    }

    @Test
    void getAccountReturnsSingleAccount() throws Exception {
        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(FROM_ACCOUNT_ID))
                .andExpect(jsonPath("$.data.balanceInCents").value(INITIAL_FROM_BALANCE));
    }

    @Test
    void successfulTransferCommitsBothBalancesAndRecord() throws Exception {
        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId":1,"toAccountId":2,"amountInCents":1000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.transferId").isNumber())
                .andExpect(jsonPath("$.data.amountInCents").value(1000));

        // 请求结束后重新查询，检查真正提交的数据，而不只检查接口返回值。
        assertThat(getBalance(FROM_ACCOUNT_ID)).isEqualTo(99_000L);
        assertThat(getBalance(TO_ACCOUNT_ID)).isEqualTo(51_000L);
        assertThat(countTransfers()).isEqualTo(1L);
    }

    @Test
    void simulatedFailureRollsBackAllThreeWrites(CapturedOutput output) throws Exception {
        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId":1,"toAccountId":2,"amountInCents":1000,"simulateFailure":true}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEMO_ROLLBACK"));

        assertBalancesUnchanged();
        assertThat(countTransfers()).isZero();
        assertThat(output.getOut()).contains("AccountController.transfer(..)", "正常返回=false", "DEMO_ROLLBACK");
    }

    @Test
    void insufficientBalanceReturnsConflictWithoutChangingData() throws Exception {
        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId":1,"toAccountId":2,"amountInCents":100001}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_BALANCE"));

        assertBalancesUnchanged();
        assertThat(countTransfers()).isZero();
    }

    @Test
    void transferToMissingAccountReturnsNotFoundWithoutDeducting() throws Exception {
        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId":1,"toAccountId":999,"amountInCents":1000}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));

        assertBalancesUnchanged();
        assertThat(countTransfers()).isZero();
    }

    @Test
    void getMissingAccountReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/accounts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void sameAccountTransferIsRejected() throws Exception {
        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId":1,"toAccountId":1,"amountInCents":1000}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SAME_ACCOUNT"));

        assertBalancesUnchanged();
        assertThat(countTransfers()).isZero();
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, 100_000_001L})
    void invalidAmountsAreRejectedBeforeBusinessLogic(long amountInCents) throws Exception {
        String json = """
                {"fromAccountId":1,"toAccountId":2,"amountInCents":%d}
                """.formatted(amountInCents);
        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("amountInCents")));

        assertBalancesUnchanged();
        assertThat(countTransfers()).isZero();
    }

    @Test
    void missingAmountIsRejected() throws Exception {
        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccountId\":1,\"toAccountId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("转账金额不能为空")));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    void invalidPathIdsReturnBadRequest(String accountId) throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", accountId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void missingRouteKeepsNotFoundStatus() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void unsupportedMethodKeepsStatusAndAllowHeader() throws Exception {
        mockMvc.perform(post("/api/accounts"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, containsString("GET")))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void unsupportedContentTypeKeepsUnsupportedMediaTypeStatus() throws Exception {
        mockMvc.perform(post("/api/transfers").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void unexpectedDatabaseFailureReturnsSafeMessageAndRollsBackDeduction() throws Exception {
        // 人为准备 BIGINT 溢出场景，验证兜底 500 和异常发生在第二次写入时的回滚。
        jdbcClient.sql("UPDATE account SET balance_in_cents = :balance WHERE id = :id")
                .param("balance", Long.MAX_VALUE).param("id", TO_ACCOUNT_ID).update();

        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId":1,"toAccountId":2,"amountInCents":1}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("服务器内部错误，请稍后重试"))
                .andExpect(jsonPath("$.message", not(containsString("SQL"))));

        assertThat(getBalance(FROM_ACCOUNT_ID)).isEqualTo(INITIAL_FROM_BALANCE);
        assertThat(getBalance(TO_ACCOUNT_ID)).isEqualTo(Long.MAX_VALUE);
        assertThat(countTransfers()).isZero();
    }

    private void assertBalancesUnchanged() {
        assertThat(getBalance(FROM_ACCOUNT_ID)).isEqualTo(INITIAL_FROM_BALANCE);
        assertThat(getBalance(TO_ACCOUNT_ID)).isEqualTo(INITIAL_TO_BALANCE);
    }

    private long getBalance(long accountId) {
        return jdbcClient.sql("SELECT balance_in_cents FROM account WHERE id = :id")
                .param("id", accountId).query(Long.class).single();
    }

    private long countTransfers() {
        return jdbcClient.sql("SELECT COUNT(*) FROM transfer_record").query(Long.class).single();
    }
}
