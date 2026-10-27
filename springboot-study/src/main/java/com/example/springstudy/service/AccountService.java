package com.example.springstudy.service;

import com.example.springstudy.dto.TransferRequest;
import com.example.springstudy.dto.TransferVO;
import com.example.springstudy.model.AccountDO;
import java.util.List;

/** 业务接口定义“能做什么”，实现类负责“怎样做”；Controller 依赖这个接口。 */
public interface AccountService {

    List<AccountDO> listAccounts();

    AccountDO getAccount(Long accountId);

    TransferVO transfer(TransferRequest request);
}
