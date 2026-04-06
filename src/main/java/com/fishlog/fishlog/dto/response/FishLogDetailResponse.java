package com.fishlog.fishlog.dto.response;

import com.fishlog.fishlog.domain.FishLog;
import com.fishlog.fishlog.domain.enums.WaterType;
import com.fishlog.fishlog.domain.enums.WeatherCondition;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "낚시 기록 상세 응답 (사진 목록 포함)")
public record FishLogDetailResponse(

        @Schema(description = "낚시 기록 ID", example = "1")
        Long id,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "기록 제목", example = "한강에서 잡은 첫 메기!")
        String title,

        @Schema(description = "사용자 직접 입력 어종명", example = "메기")
        String fishName,

        @Schema(description = "AI 분류 어종명 (Gemini Vision 결과)", example = "메기")
        String fishNameAi,

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

        @Schema(description = "사용한 미끼", example = "지렁이")
        String bait,

        @Schema(description = "날씨", example = "SUNNY")
        WeatherCondition weather,

        @Schema(description = "기온 (℃)", example = "24.0")
        Double temperature,

        @Schema(description = "메모", example = "오전에 갔더니 입질이 잘 왔음.")
        String memo,

        @Schema(description = "첨부 사진 목록")
        List<FishPhotoResponse> photos,

        @Schema(description = "기록 생성 일시", example = "2025-07-20T09:30:00")
        LocalDateTime createdAt,

        @Schema(description = "기록 수정 일시", example = "2025-07-20T10:00:00")
        LocalDateTime updatedAt
) {
    public static FishLogDetailResponse from(FishLog fishLog) {
        List<FishPhotoResponse> photos = fishLog.getPhotos().stream()
                .map(FishPhotoResponse::from)
                .toList();

        return new FishLogDetailResponse(
                fishLog.getId(),
                fishLog.getUser().getId(),
                fishLog.getTitle(),
                fishLog.getFishName(),
                fishLog.getFishNameAi(),
                fishLog.getCatchDate(),
                fishLog.getLocation(),
                fishLog.getWaterType(),
                fishLog.getLengthCm(),
                fishLog.getWeightG(),
                fishLog.getBait(),
                fishLog.getWeather(),
                fishLog.getTemperature(),
                fishLog.getMemo(),
                photos,
                fishLog.getCreatedAt(),
                fishLog.getUpdatedAt()
        );
    }
}
