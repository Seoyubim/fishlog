package com.fishlog.fishlog.dto.response.stats;

import com.fishlog.fishlog.domain.enums.WaterType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수계별 포획 통계")
public record WaterTypeStatDto(

        @Schema(description = "수계 종류 (FRESHWATER: 민물, SEA: 바다)", example = "FRESHWATER")
        WaterType waterType,

        @Schema(description = "해당 수계 포획 횟수", example = "31")
        long count
) {}
