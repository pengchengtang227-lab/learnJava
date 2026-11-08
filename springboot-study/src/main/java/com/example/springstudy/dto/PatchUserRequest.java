package com.example.springstudy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 动态 SQL 部分更新请求：只提交需要修改的字段，省略或 null 表示保留原值。
 * 与完整更新的 SaveUserRequest 分开，避免把未提交的年龄、性别覆盖成默认值。
 */
@Schema(description = "部分更新用户：至少提供一个非 null 字段，省略或 null 表示不修改",
        example = "{\"email\":\"alice.new@example.com\"}")
public record PatchUserRequest(
        @Schema(description = "新用户名；省略或 null 表示不修改", example = "alice")
        @Size(max = 50, message = "用户名最多 50 个字符")
        @Pattern(regexp = "(?s).*\\S.*", message = "用户名不能是空字符串或空白")
        String userName,

        @Schema(description = "新年龄，范围 0～150；省略或 null 表示不修改", example = "21")
        @Min(value = 0, message = "年龄不能小于 0")
        @Max(value = 150, message = "年龄不能大于 150")
        Integer age,

        @Schema(description = "新邮箱；省略或 null 表示不修改", example = "alice.new@example.com")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱最多 100 个字符")
        @Pattern(regexp = "(?s).*\\S.*", message = "邮箱不能是空字符串或空白")
        String email,

        @Schema(description = "新性别：0 未知，1 男，2 女；省略或 null 表示不修改", example = "0")
        @Min(value = 0, message = "性别只能为 0（未知）、1（男）、2（女）")
        @Max(value = 2, message = "性别只能为 0（未知）、1（男）、2（女）")
        Integer gender) {
}
