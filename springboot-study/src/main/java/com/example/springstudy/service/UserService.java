package com.example.springstudy.service;

import com.example.springstudy.dto.SaveUserRequest;
import com.example.springstudy.model.UserDO;
import java.util.List;

/** 用户 CRUD 业务接口；Controller 依赖此接口，不直接操作 Mapper。 */
public interface UserService {

    List<UserDO> listUsers();

    UserDO getUser(Long userId);

    UserDO saveUser(SaveUserRequest request);

    /** 返回更新后的完整用户数据；用户不存在时抛出业务异常。 */
    UserDO updateUser(Long userId, SaveUserRequest request);

    void removeUser(Long userId);
}
