package com.example.springstudy.controller;

import com.example.springstudy.common.ApiResponse;
import com.example.springstudy.dto.BatchSaveUserRequest;
import com.example.springstudy.dto.SaveUserRequest;
import com.example.springstudy.model.UserDO;
import com.example.springstudy.service.UserService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * Read：查询一页用户。省略参数时查询第 1 页、每页 10 条；非法参数由 MVC 校验返回 400。
     * data.list 是本页用户，data.total 是总记录数，data.pages 是总页数。
     */
    @GetMapping
    @Operation(summary = "分页查询用户列表：PageHelper",
            description = "按 ID 升序；默认第 1 页、每页 10 条，最多 100 条。data.list 为用户列表，total 为总数，pages 为总页数。")
    public ApiResponse<PageInfo<UserDO>> listUsers(
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(name = "pageNum", defaultValue = "1")
            @Min(value = 1, message = "页码必须大于等于 1") int pageNum,
            @Parameter(description = "每页条数，范围 1～100", example = "10")
            @RequestParam(name = "pageSize", defaultValue = "10")
            @Min(value = 1, message = "每页条数必须大于等于 1")
            @Max(value = 100, message = "每页条数不能超过 100") int pageSize) {
        return ApiResponse.success(userService.listUsers(pageNum, pageSize));
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

    /** 批量 Create：先校验整个列表，再由 Service 分组，XML foreach 生成多行 INSERT。 */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "批量新增用户：分组 + foreach",
            description = "一次 1～10000 条，每组最多 500 条；全部成功返回插入数量，任意一组失败则全部回滚。")
    public ApiResponse<Integer> saveUsers(@Valid @RequestBody BatchSaveUserRequest request) {
        return ApiResponse.success(userService.saveUsers(request.users()));
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
