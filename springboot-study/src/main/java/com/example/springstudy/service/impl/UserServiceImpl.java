package com.example.springstudy.service.impl;

import com.example.springstudy.dto.SaveUserRequest;
import com.example.springstudy.exception.BusinessException;
import com.example.springstudy.mapper.UserMapper;
import com.example.springstudy.model.UserDO;
import com.example.springstudy.service.UserService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务层负责存在性检查和事务；Mapper 只负责执行 SQL。
 * 构造器注入的 Mapper 是 MyBatis 代理对象，参与 Spring 管理的数据源和事务。
 */
@Service
public class UserServiceImpl implements UserService {

    private static final int GENDER_UNKNOWN = 0;

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<UserDO> listUsers() {
        return userMapper.listUsers();
    }

    @Override
    public UserDO getUser(Long userId) {
        // 单条查询无结果时返回 null，统一转换成 404 业务异常。
        return Optional.ofNullable(userMapper.getUserById(userId))
                .orElseThrow(() -> userNotFound(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDO saveUser(SaveUserRequest request) {
        UserDO user = toUser(request);
        userMapper.insertUser(user);
        // user.id 已回填；再查询可获取数据库自动生成的创建和修改时间。
        return getUser(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDO updateUser(Long userId, SaveUserRequest request) {
        UserDO user = toUser(request);
        user.setId(userId);
        // 通过更新匹配行数判断用户是否存在，无需提前查询。
        if (userMapper.updateUser(user) == 0) {
            throw userNotFound(userId);
        }
        // 查询更新后的完整数据，包含数据库维护的创建时间和修改时间。
        return getUser(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUser(Long userId) {
        // 先查询确认目标，再按主键删除；重复删除返回 404。
        getUser(userId);
        if (userMapper.deleteUser(userId) == 0) {
            throw userNotFound(userId);
        }
    }

    private UserDO toUser(SaveUserRequest request) {
        UserDO user = new UserDO();
        user.setUserName(request.userName().trim());
        user.setAge(request.age());
        user.setEmail(request.email());
        user.setGender(Objects.requireNonNullElse(request.gender(), GENDER_UNKNOWN));
        return user;
    }

    private BusinessException userNotFound(Long userId) {
        return new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在：" + userId);
    }
}
