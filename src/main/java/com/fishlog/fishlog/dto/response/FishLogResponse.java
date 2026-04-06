package com.fishlog.fishlog.dto.response;

import com.fishlog.fishlog.domain.FishLog;
import com.fishlog.fishlog.domain.enums.WaterType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "낚시 기록 목록 응답 (요약 정보)")
public record FishLogResponse(

        @Schema(description = "낚시 기록 ID", example = "1")
        Long id,

        @Schema(description = "기록 제목", example = "한강에서 잡은 첫 메기!")
        String title,

        @Schema(description = "어종명 (AI 분류명 우선, 없으면 직접 입력값)", example = "메기")
        String fishName,

        @Schema(description = "낚시한 날짜", example = "2025-07-20")
        LocalDate catchDate,

        @Schema(description = "낚시 장소", example = "서울 한강 난지공원")
        String location,

        @Schema(description = "수계 종류", example = "FRESHWATER")
        WaterType waterType,

        @Schema(description = "어체장 (cm)", example = "42.5")
        Double lengthCm,

        @Schema(description = "무게 (g)", example = "1200.0")
        Double weightG,

        @Schema(description = "첨부 사진 수", example = "2")
        int photoCount,

        @Schema(description = "기록 생성 일시", example = "2025-07-20T09:30:00")
        LocalDateTime createdAt
) {
    public static FishLogResponse from(FishLog fishLog) {
        String displayFishName = fishLog.getFishNameAi() != null
                ? fishLog.getFishNameAi()
                : fishLog.getFishName();

        return new FishLogResponse(
                fishLog.getId(),
                fishLog.getTitle(),
                displayFishName,
                fishLog.getCatchDate(),
                fishLog.getLocation(),
                fishLog.getWaterType(),
                fishLog.getLengthCm(),
                fishLog.getWeightG(),
                fishLog.getPhotos().size(),
                fishLog.getCreatedAt()
        );
    }
}
