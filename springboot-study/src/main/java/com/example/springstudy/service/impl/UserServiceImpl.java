package com.example.springstudy.service.impl;

import com.example.springstudy.dto.PatchUserRequest;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    public UserDO getUserRelations(Long userId) {
        // LEFT JOIN 保留没有资料、没有订单的用户，只有用户本身不存在才返回 404。
        return Optional.ofNullable(userMapper.getUserRelations(userId))
                .orElseThrow(() -> userNotFound(userId));
    }

    @Override
    public UserDO getUserRelationsByNestedSelect(Long userId) {
        // 只调用主查询；MyBatis 根据 XML 的 select 自动查询资料和订单并填入 UserDO。
        // 用户存在但没有关联数据时，仍正常返回，profile 为 null、orders 为空列表。
        return Optional.ofNullable(userMapper.getUserRelationsByNestedSelect(userId))
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
        // 同一事务内先清理关联数据，避免删除用户后留下无主的资料或订单。
        userMapper.deleteUserOrders(userId);
        userMapper.deleteUserProfile(userId);
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

    @Override
    public List<UserDO> searchUsers(String userName, String namePrefix, Integer age) {
        // 空字符串和纯空白都视为未填写，使 Swagger 中留空的可选条件符合预期。
        String normalizedUserName = StringUtils.hasText(userName) ? userName.trim() : null;
        String normalizedNamePrefix = StringUtils.hasText(namePrefix) ? namePrefix.trim() : null;
        return userMapper.searchUsers(normalizedUserName, normalizedNamePrefix, age);
    }

    @Override
    public List<UserDO> listUsersByIds(List<Long> userIds) {
        // 空集合没有查询目标，直接返回；XML 也保留空集合分支，便于直接调试 Mapper。
        if (CollectionUtils.isEmpty(userIds)) {
            return List.of();
        }
        return userMapper.listUsersByIds(userIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDO patchUser(Long userId, PatchUserRequest request) {
        // null 代表“不修改”；至少提交一个字段，避免只有修改时间变化的空更新。
        if (Objects.isNull(request.userName()) && Objects.isNull(request.age())
                && Objects.isNull(request.email()) && Objects.isNull(request.gender())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "至少提供一个需要修改的字段");
        }
        getUser(userId);
        UserDO user = new UserDO();
        user.setId(userId);
        user.setUserName(Objects.nonNull(request.userName()) ? request.userName().trim() : null);
        user.setAge(request.age());
        user.setEmail(request.email());
        user.setGender(request.gender());
        if (userMapper.updateUserSelective(user) == 0) {
            throw userNotFound(userId);
        }
        // 重新查询，让返回值包含未修改的字段和数据库维护的创建、修改时间。
        return getUser(userId);
    }

    private BusinessException userNotFound(Long userId) {
        return new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在：" + userId);
    }
}
