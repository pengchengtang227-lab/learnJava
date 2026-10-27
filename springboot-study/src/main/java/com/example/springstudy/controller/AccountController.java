package com.example.springstudy.controller;

import com.example.springstudy.common.ApiResponse;
import com.example.springstudy.dto.TransferRequest;
import com.example.springstudy.dto.TransferVO;
import com.example.springstudy.model.AccountDO;
import com.example.springstudy.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
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

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/accounts")
    public ApiResponse<List<AccountDO>> listAccounts() {
        return ApiResponse.success(accountService.listAccounts());
    }

    // 路径参数示例：GET /api/accounts/1。Spring MVC 自动执行 @Positive 校验。
    @GetMapping("/accounts/{id}")
    public ApiResponse<AccountDO> getAccount(@PathVariable("id") @Positive Long accountId) {
        return ApiResponse.success(accountService.getAccount(accountId));
    }

    // @RequestBody 将 JSON 转为 Java 对象，@Valid 触发对象字段上的约束。
    @PostMapping("/transfers")
    public ApiResponse<TransferVO> transfer(@Valid @RequestBody TransferRequest request) {
        return ApiResponse.success(accountService.transfer(request));
    }
}
