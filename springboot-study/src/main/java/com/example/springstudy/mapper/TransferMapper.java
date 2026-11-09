package com.example.springstudy.mapper;

import com.example.springstudy.model.TransferRecordDO;
import org.apache.ibatis.annotations.Mapper;

/** 转账记录与账户 Mapper 使用同一数据源，参与 Service 开启的本地事务。 */
@Mapper
public interface TransferMapper {

    /** 返回插入行数；自增主键由 useGeneratedKeys 回填到 transferRecord.id。 */
    int insertTransfer(TransferRecordDO transferRecord);
}
