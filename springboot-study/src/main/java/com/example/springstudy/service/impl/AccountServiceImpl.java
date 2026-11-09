package com.example.springstudy.service.impl;

import com.example.springstudy.dto.TransferRequest;
import com.example.springstudy.dto.TransferVO;
import com.example.springstudy.exception.BusinessException;
import com.example.springstudy.mapper.AccountMapper;
import com.example.springstudy.mapper.TransferMapper;
import com.example.springstudy.model.AccountDO;
import com.example.springstudy.model.TransferRecordDO;
import com.example.springstudy.service.AccountService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 业务层组织多个数据库操作，事务边界应放在这一层，而不是 Controller。 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final TransferMapper transferMapper;

    // 只有一个构造器时无需 @Autowired，Spring 自动注入两个 MyBatis Mapper 代理。
    public AccountServiceImpl(AccountMapper accountMapper, TransferMapper transferMapper) {
        this.accountMapper = accountMapper;
        this.transferMapper = transferMapper;
    }

    @Override
    public List<AccountDO> listAccounts() {
        return accountMapper.listAccounts();
    }

    @Override
    public AccountDO getAccount(Long accountId) {
        return Optional.ofNullable(accountMapper.getAccountById(accountId))
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "ACCOUNT_NOT_FOUND", "账户不存在：" + accountId));
    }

    /**
     * Controller 调用的是 Spring 管理的代理，代理会在进入方法前开启事务。
     * 正常返回后提交；异常离开方法时，根据回滚规则决定是否回滚。
     * 默认只对 RuntimeException 和 Error 回滚，这里显式包含受检 Exception，便于学习。
     * 注意：同类内部通过 this.transfer(...) 调用会绕过代理，不能依靠它开启事务。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferVO transfer(TransferRequest request) {
        if (Objects.equals(request.fromAccountId(), request.toAccountId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SAME_ACCOUNT", "转出和转入账户不能相同");
        }
        // 先确认账户存在，避免把不存在的账户误判为余额不足。
        getAccount(request.fromAccountId());
        getAccount(request.toAccountId());

        // 下列三次写入参与同一个事务：扣款、入账、保存转账记录。
        if (accountMapper.deductBalance(request.fromAccountId(), request.amountInCents()) == 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "INSUFFICIENT_BALANCE", "账户余额不足");
        }
        if (accountMapper.addBalance(request.toAccountId(), request.amountInCents()) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "转入账户不存在");
        }
        TransferRecordDO transferRecord = new TransferRecordDO();
        transferRecord.setFromAccountId(request.fromAccountId());
        transferRecord.setToAccountId(request.toAccountId());
        transferRecord.setAmountInCents(request.amountInCents());
        transferMapper.insertTransfer(transferRecord);
        Long transferId = Objects.requireNonNull(transferRecord.getId(), "转账记录主键未生成");

        // 此时 SQL 已执行，但还没有提交。主动失败用于验证三次写入能否一起撤销。
        if (Boolean.TRUE.equals(request.simulateFailure())) {
            throw new BusinessException(HttpStatus.CONFLICT, "DEMO_ROLLBACK", "模拟转账失败，所有数据库修改已回滚");
        }

        // 不在这里 catch 后吞掉异常，否则事务代理可能看到“正常返回”而提交。
        return new TransferVO(transferId, request.fromAccountId(), request.toAccountId(), request.amountInCents());
    }
}
