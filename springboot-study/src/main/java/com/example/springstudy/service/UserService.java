package com.example.springstudy.service;

import com.example.springstudy.dto.PatchUserRequest;
import com.example.springstudy.dto.SaveUserRequest;
import com.example.springstudy.model.UserDO;
import java.util.List;

/** 用户 CRUD 业务接口；Controller 依赖此接口，不直接操作 Mapper。 */
public interface UserService {

    List<UserDO> listUsers();

    UserDO getUser(Long userId);

    /** 查询用户、单个资料和多个订单；用户不存在时返回 404。 */
    UserDO getUserRelations(Long userId);

    /** 用嵌套查询加载相同的用户资料和订单，便于对照 JOIN 的嵌套结果映射。 */
    UserDO getUserRelationsByNestedSelect(Long userId);

    UserDO saveUser(SaveUserRequest request);

    /** 分组批量新增，返回总插入数量；任意一组失败，整个请求回滚。 */
    int saveUsers(List<SaveUserRequest> requests);

    /** 返回更新后的完整用户数据；用户不存在时抛出业务异常。 */
    UserDO updateUser(Long userId, SaveUserRequest request);

    void removeUser(Long userId);

    /** 动态 SQL 条件查询；姓名优先于前缀，年龄可独立组合。 */
    List<UserDO> searchUsers(String userName, String namePrefix, Integer age);

    /** 多个 ID 查询；null/空集合返回空结果。 */
    List<UserDO> listUsersByIds(List<Long> userIds);

    /** 部分更新；空请求返回 400，不存在返回 404，成功返回完整用户数据。 */
    UserDO patchUser(Long userId, PatchUserRequest request);
}
