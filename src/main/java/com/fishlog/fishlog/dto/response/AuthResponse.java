package com.fishlog.fishlog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 응답 (JWT 토큰)")
public record AuthResponse(

        @Schema(description = "JWT Access Token")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "사용자명", example = "fisherman01")
        String username
) {
    public static AuthResponse of(String accessToken, Long userId, String username) {
        return new AuthResponse(accessToken, "Bearer", userId, username);
    }
}
