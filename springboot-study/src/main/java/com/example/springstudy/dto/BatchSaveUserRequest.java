package com.example.springstudy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 批量新增用户；复用单条新增的字段校验，限制一次请求的数据量。 */
@Schema(description = "批量新增用户，每组 500 条执行一条 INSERT，全部成功才提交")
public record BatchSaveUserRequest(
        @Schema(description = "用户列表，至少 1 条、最多 10000 条")
        @NotEmpty(message = "用户列表不能为空")
        @Size(max = BatchSaveUserRequest.MAX_USER_COUNT, message = "一次最多新增 10000 个用户")
        List<@NotNull(message = "用户列表不能包含 null") @Valid SaveUserRequest> users) {

    public static final int MAX_USER_COUNT = 10_000;
}
