package com.fishlog.fishlog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(

        @Schema(description = "사용자명", example = "fisherman01")
        @NotBlank(message = "사용자명은 필수입니다")
        String username,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) {}
