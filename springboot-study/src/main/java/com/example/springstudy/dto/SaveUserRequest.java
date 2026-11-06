package com.example.springstudy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 新增和 PUT 更新共用的请求对象；PUT 要求提交完整的可编辑字段。
 * ID 从路径获取，创建/修改时间由数据库维护，避免客户端覆盖这些字段。
 * Controller 上的 @Valid 会在调用 Service 前触发这里的约束。
 */
@Schema(description = "用户新增或完整更新的请求字段")
public record SaveUserRequest(
        @Schema(description = "用户名，不能重复，最多 50 个字符", example = "zhangsan")
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名最多 50 个字符")
        String userName,

        @Schema(description = "年龄，范围 0～150", example = "20")
        @NotNull(message = "年龄不能为空")
        @Min(value = 0, message = "年龄不能小于 0")
        @Max(value = 150, message = "年龄不能大于 150")
        Integer age,

        @Schema(description = "邮箱地址", example = "zhangsan@example.com")
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱最多 100 个字符")
        String email,

        @Schema(description = "性别：0 未知，1 男，2 女；省略或为 null 时按 0 保存",
                example = "1", allowableValues = {"0", "1", "2"}, defaultValue = "0")
        @Min(value = 0, message = "性别只能为 0（未知）、1（男）、2（女）")
        @Max(value = 2, message = "性别只能为 0（未知）、1（男）、2（女）")
        Integer gender) {
}
