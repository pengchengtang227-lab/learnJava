package com.example.springstudy.controller;

import com.example.springstudy.common.ApiResponse;
import com.example.springstudy.model.UserDO;
import com.example.springstudy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用两个只读接口对照嵌套结果映射（JOIN）与嵌套查询（select），返回相同的对象结构。 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "MyBatis 关联查询", description = "用户资料一对一、用户订单一对多")
public class UserRelationController {

    private final UserService userService;

    public UserRelationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/relations")
    @Operation(summary = "嵌套结果映射：JOIN 查询用户资料和订单",
            description = "先在用户列表找到 association-demo 的实际 ID，再查询。profile 是对象，orders 是数组；没有关联数据时分别为 null 和 []。")
    public ApiResponse<UserDO> getUserRelations(
            @Parameter(description = "用户 ID，可使用 association-demo 的实际 ID", example = "1")
            @PathVariable("id") @Positive Long userId) {
        return ApiResponse.success(userService.getUserRelations(userId));
    }

    /** 先查用户，再由 MyBatis 执行资料和订单查询；当前示例立即加载，便于观察三次 SQL。 */
    @GetMapping("/{id}/relations/nested-select")
    @Operation(summary = "嵌套查询：通过 select 分别查询资料和订单",
            description = "与 JOIN 接口使用相同用户 ID。立即加载，用户存在且无缓存命中时执行 3 次 SQL；响应结构相同。")
    public ApiResponse<UserDO> getUserRelationsByNestedSelect(
            @Parameter(description = "用户 ID，可使用 association-demo 的实际 ID", example = "1")
            @PathVariable("id") @Positive Long userId) {
        return ApiResponse.success(userService.getUserRelationsByNestedSelect(userId));
    }
}
