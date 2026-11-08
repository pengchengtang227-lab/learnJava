package com.example.springstudy.controller;

import com.example.springstudy.common.ApiResponse;
import com.example.springstudy.dto.PatchUserRequest;
import com.example.springstudy.model.UserDO;
import com.example.springstudy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用三个 HTTP 接口练习常见动态 SQL 标签；Swagger 中单独归到“MyBatis 动态 SQL”。
 * 对照 Controller 参数、Mapper 的 @Param 和 XML 标签，再观察控制台的 Preparing/Parameters。
 */
@RestController
@RequestMapping("/api/users/dynamic-sql")
@Tag(name = "MyBatis 动态 SQL", description = "三个例子覆盖条件查询、集合查询和部分更新")
public class UserDynamicSqlController {

    private static final int MAX_QUERY_IDS = 1000;

    private final UserService userService;

    public UserDynamicSqlController(UserService userService) {
        this.userService = userService;
    }

    /** choose 在精确姓名和前缀查询之间选择；if 追加年龄条件，where 整理条件，bind 创建 LIKE 参数。 */
    @GetMapping("/search")
    @Operation(summary = "动态条件查询：if / where / choose / bind",
            description = "userName 优先做精确匹配；未填写时才使用 namePrefix 前缀匹配。age 可独立组合；全部省略查询全部用户。")
    public ApiResponse<List<UserDO>> searchUsers(
            @Parameter(description = "精确用户名，优先于 namePrefix", example = "alice")
            @RequestParam(name = "userName", required = false)
            @Size(max = 50, message = "用户名最多 50 个字符") String userName,
            @Parameter(description = "用户名 LIKE 前缀，例如 ali 可匹配 alice；% 和 _ 保留通配符含义", example = "ali")
            @RequestParam(name = "namePrefix", required = false)
            @Size(max = 50, message = "用户名前缀最多 50 个字符") String namePrefix,
            @Parameter(description = "年龄，可与姓名条件组合，范围 0～150", example = "20")
            @RequestParam(name = "age", required = false)
            @Min(value = 0, message = "年龄不能小于 0")
            @Max(value = 150, message = "年龄不能大于 150") Integer age) {
        return ApiResponse.success(userService.searchUsers(userName, namePrefix, age));
    }

    /** trim 整理 WHERE/AND，foreach 将 ID 列表展开为 IN (?, ?, ...)。 */
    @GetMapping("/by-ids")
    @Operation(summary = "按多个 ID 查询：trim / foreach",
            description = "例如 ids=1,2,3，也支持重复的 ids 参数；按 ID 排序。省略或传入空列表时返回空结果。")
    public ApiResponse<List<UserDO>> listUsersByIds(
            @Parameter(description = "用户 ID 列表，每项为正数，最多 1000 项")
            @RequestParam(name = "ids", required = false)
            @Size(max = MAX_QUERY_IDS, message = "一次最多查询 1000 个 ID")
            List<@NotNull(message = "用户 ID 不能为空") @Positive(message = "用户 ID 必须为正数") Long> userIds) {
        return ApiResponse.success(userService.listUsersByIds(userIds));
    }

    /** PATCH 只提交要改的字段，XML 用 if + set 组合 SET 子句，并更新 gmt_modified。 */
    @PatchMapping("/{id}")
    @Operation(summary = "部分更新用户：if / set",
            description = "至少提交一个非 null 字段，例如只传 email；其余字段保留原值。空更新返回 400，用户不存在返回 404。")
    public ApiResponse<UserDO> patchUser(
            @Parameter(description = "要修改的实际用户 ID", example = "1")
            @PathVariable("id") @Positive Long userId,
            @Valid @RequestBody PatchUserRequest request) {
        return ApiResponse.success(userService.patchUser(userId, request));
    }
}
