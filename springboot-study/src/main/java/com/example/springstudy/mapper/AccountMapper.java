package com.example.springstudy.mapper;

import com.example.springstudy.model.AccountDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 账户数据访问；MyBatis 创建接口代理，SQL 和 record 构造器映射见 AccountMapper.xml。 */
@Mapper
public interface AccountMapper {

    List<AccountDO> listAccounts();

    AccountDO getAccountById(@Param("id") Long accountId);

    /** 返回更新行数；余额检查与扣款在同一条 SQL 中完成。 */
    int deductBalance(@Param("id") Long accountId, @Param("amount") Long amountInCents);

    int addBalance(@Param("id") Long accountId, @Param("amount") Long amountInCents);
}
