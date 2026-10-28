package com.example.springstudy.controller;

import com.example.springstudy.common.ApiResponse;
import com.example.springstudy.dto.TransferRequest;
import com.example.springstudy.dto.TransferVO;
import com.example.springstudy.model.AccountDO;
import com.example.springstudy.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web 层负责接收请求、校验参数、调用业务层、返回数据。
 * @RestController = @Controller + @ResponseBody，返回对象会被转换为 JSON。
 */
@RestController
@RequestMapping("/api")
public class AccountController {

    // 业务日志：记录“业务上发生了什么”。方法的开始/结束/耗时已由 ApiLogAspect 统一记录，这里不重复。
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/accounts")
    public ApiResponse<List<AccountDO>> listAccounts() {
        List<AccountDO> accounts = accountService.listAccounts();
        LOGGER.info("查询账户列表：共 {} 个账户", accounts.size());
        return ApiResponse.success(accounts);
    }

    // 路径参数示例：GET /api/accounts/1。Spring MVC 自动执行 @Positive 校验。
    @GetMapping("/accounts/{id}")
    public ApiResponse<AccountDO> getAccount(@PathVariable("id") @Positive Long accountId) {
        // {} 是占位符，参数不会提前拼接字符串，日志级别未开启时几乎没有开销。
        LOGGER.info("查询账户详情：accountId={}", accountId);
        return ApiResponse.success(accountService.getAccount(accountId));
    }

    // @RequestBody 将 JSON 转为 Java 对象，@Valid 触发对象字段上的约束。
    @PostMapping("/transfers")
    public ApiResponse<TransferVO> transfer(@Valid @RequestBody TransferRequest request) {
        // 走到这里说明参数校验已通过。只记录账户 ID 和金额等排查需要的字段，不打印整个请求对象。
        LOGGER.info("收到转账请求：from={}，to={}，金额={}分，模拟失败={}",
                request.fromAccountId(), request.toAccountId(), request.amountInCents(), request.simulateFailure());
        TransferVO result = accountService.transfer(request);
        LOGGER.info("转账完成：transferId={}", result.transferId());
        return ApiResponse.success(result);
    }
}
