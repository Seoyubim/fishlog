package com.fishlog.fishlog.dto.request;

import com.fishlog.fishlog.domain.enums.WaterType;
import com.fishlog.fishlog.domain.enums.WeatherCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "낚시 기록 수정 요청 (모든 필드 전송 필요)")
public record FishLogUpdateRequest(

        @Schema(description = "기록 제목", example = "한강 메기 낚시 (수정)")
        @NotBlank(message = "제목은 필수입니다")
        String title,

        @Schema(description = "사용자가 직접 입력한 어종명", example = "메기")
        String fishName,

        @Schema(description = "낚시한 날짜 (오늘 이전)", example = "2025-07-20")
        @NotNull(message = "낚은 날짜는 필수입니다")
        @PastOrPresent(message = "낚은 날짜는 오늘 이전이어야 합니다")
        LocalDate catchDate,

        @Schema(description = "낚시 장소", example = "서울 한강 난지공원")
        String location,

        @Schema(description = "수계 종류 (FRESHWATER: 민물, SEA: 바다)", example = "FRESHWATER")
        WaterType waterType,

        @Schema(description = "어체장 (cm)", example = "42.5")
        @Positive(message = "어체장은 양수여야 합니다")
        Double lengthCm,

        @Schema(description = "무게 (g)", example = "1200.0")
        @Positive(message = "무게는 양수여야 합니다")
        Double weightG,

        @Schema(description = "사용한 미끼", example = "지렁이")
        String bait,

        @Schema(description = "날씨 (SUNNY/CLOUDY/RAINY/WINDY/SNOWY)", example = "CLOUDY")
        WeatherCondition weather,

        @Schema(description = "기온 (℃)", example = "22.0")
        Double temperature,

        @Schema(description = "메모", example = "흐린 날씨였는데도 잘 잡혔음.")
        String memo
) {}
