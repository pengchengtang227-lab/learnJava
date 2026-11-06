package com.example.springstudy.controller;

import com.example.springstudy.common.ApiResponse;
import com.example.springstudy.dto.SaveUserRequest;
import com.example.springstudy.model.UserDO;
import com.example.springstudy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户 REST 接口：HTTP 请求 → 参数校验 → Service → Mapper → 数据库。
 * 复用项目已有的 ApiResponse、AOP 日志和全局异常处理。
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "MyBatis 用户 CRUD，可直接填写 JSON 调用")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Read：查询列表；当前学习示例直接返回全部用户。 */
    @GetMapping
    @Operation(summary = "查询用户列表", description = "按 ID 排序，当前示例返回全部用户。")
    public ApiResponse<List<UserDO>> listUsers() {
        return ApiResponse.success(userService.listUsers());
    }

    /** Read：路径中的 ID 必须为正数，不存在时返回 404。 */
    @GetMapping("/{id}")
    @Operation(summary = "查询用户详情", description = "填写实际用户 ID；不存在时返回 404。")
    public ApiResponse<UserDO> getUser(
            @Parameter(description = "用户 ID，使用新增接口返回的 data.id", example = "1")
            @PathVariable("id") @Positive Long userId) {
        return ApiResponse.success(userService.getUser(userId));
    }

    /** Create：@RequestBody 读取 JSON，@Valid 触发 DTO 校验；成功返回 201。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增用户", description = "用户名不能重复；成功返回 201，data.id 为数据库生成的主键。")
    public ResponseEntity<ApiResponse<UserDO>> saveUser(@Valid @RequestBody SaveUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userService.saveUser(request)));
    }

    /** Update：PUT 提交完整的可编辑字段；成功返回 200 和更新后的完整用户数据。 */
    @PutMapping("/{id}")
    @Operation(summary = "修改用户", description = "提交用户名、年龄、邮箱和性别；性别省略时设为未知。成功返回更新后的完整用户数据；用户不存在则返回 404。")
    public ApiResponse<UserDO> updateUser(
            @Parameter(description = "要修改的用户 ID", example = "1")
            @PathVariable("id") @Positive Long userId,
            @Valid @RequestBody SaveUserRequest request) {
        return ApiResponse.success(userService.updateUser(userId, request));
    }

    /** Delete：删除成功返回 200，响应 data 为 null。 */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "按 ID 物理删除用户；成功返回 200，data 为 null。")
    public ApiResponse<Void> removeUser(
            @Parameter(description = "要删除的用户 ID", example = "1")
            @PathVariable("id") @Positive Long userId) {
        userService.removeUser(userId);
        return ApiResponse.success(null);
    }
}
