package com.fishlog.fishlog.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "공통 에러 응답")
public record ErrorResponse(

        @Schema(description = "HTTP 상태 코드", example = "404")
        int status,

        @Schema(description = "에러 메시지", example = "FishLog를 찾을 수 없습니다. id=99")
        String message,

        @Schema(description = "에러 발생 일시", example = "2025-07-20T09:30:00")
        LocalDateTime timestamp
) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, LocalDateTime.now());
    }
}
