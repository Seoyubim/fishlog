package com.fishlog.fishlog.dto.response;

import com.fishlog.fishlog.domain.FishPhoto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "낚시 사진 응답")
public record FishPhotoResponse(

        @Schema(description = "사진 ID", example = "1")
        Long id,

        @Schema(description = "사진 파일 URL 또는 저장 경로", example = "uploads/20250720_abc123.jpg")
        String photoUrl,

        @Schema(description = "AI 어종 분류 완료 여부", example = "true")
        boolean aiClassified,

        @Schema(description = "AI 분류 신뢰도 (0.0 ~ 1.0, 미분류 시 null)", example = "0.92")
        Double aiConfidence,

        @Schema(description = "사진 업로드 일시", example = "2025-07-20T09:35:00")
        LocalDateTime createdAt
) {
    public static FishPhotoResponse from(FishPhoto photo) {
        return new FishPhotoResponse(
                photo.getId(),
                photo.getPhotoUrl(),
                photo.isAiClassified(),
                photo.getAiConfidence(),
                photo.getCreatedAt()
        );
    }
}
