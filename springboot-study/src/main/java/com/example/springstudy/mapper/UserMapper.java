package com.example.springstudy.mapper;

import com.example.springstudy.model.UserDO;
import com.example.springstudy.model.UserOrderDO;
import com.example.springstudy.model.UserProfileDO;
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

    /** 联表查询：association 映射单个资料对象，collection 映射订单列表。 */
    UserDO getUserRelations(@Param("id") Long userId);

    /** 嵌套查询：先查用户，由 resultMap 中的 select 自动调用下面两个关联查询。 */
    UserDO getUserRelationsByNestedSelect(@Param("id") Long userId);

    /** association 的子查询：XML 把主查询的 id 列传为 userId，查询一份资料。 */
    UserProfileDO getUserProfileByUserId(@Param("userId") Long userId);

    /** collection 的子查询：接收同一个 userId，查询该用户的订单列表。 */
    List<UserOrderDO> listUserOrdersByUserId(@Param("userId") Long userId);

    // 返回受影响行数；生成的主键写回 user.id，二者不是同一个结果。
    int insertUser(UserDO user);

    int updateUser(UserDO user);

    int deleteUser(@Param("id") Long userId);

    int deleteUserProfile(@Param("id") Long userId);

    int deleteUserOrders(@Param("id") Long userId);

    /** 动态查询：精确姓名优先，否则用前缀；年龄条件可以独立组合。 */
    List<UserDO> searchUsers(@Param("userName") String userName, @Param("namePrefix") String namePrefix,
            @Param("age") Integer age);

    /** foreach 的 collection="ids" 对应 @Param("ids")，而不是 Java 变量名 userIds。 */
    List<UserDO> listUsersByIds(@Param("ids") List<Long> userIds);

    /** 非 null 字段参与更新；返回影响行数，更新后的完整数据由 Service 再查询。 */
    int updateUserSelective(UserDO user);
}
