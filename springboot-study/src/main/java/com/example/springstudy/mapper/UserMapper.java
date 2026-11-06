package com.example.springstudy.mapper;

import com.example.springstudy.model.UserDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 数据访问层只定义操作，具体 SQL 放在 resources/mapper/UserMapper.xml。
 * @Mapper 让 MyBatis 为接口创建代理 Bean，无需手写实现类。
 * XML namespace 对应本接口全限定名，SQL id 对应方法名。
 */
@Mapper
public interface UserMapper {

    List<UserDO> listUsers();

    // @Param 明确 XML 的 #{id} 对应哪个参数，避免依赖编译器保留参数名。
    UserDO getUserById(@Param("id") Long userId);

    // 返回受影响行数；生成的主键写回 user.id，二者不是同一个结果。
    int insertUser(UserDO user);

    int updateUser(UserDO user);

    int deleteUser(@Param("id") Long userId);
}
