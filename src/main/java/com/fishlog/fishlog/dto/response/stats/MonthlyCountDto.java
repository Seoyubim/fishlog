package com.fishlog.fishlog.dto.response.stats;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "월별 포획 마릿수")
public record MonthlyCountDto(

        @Schema(description = "월 (1~12)", example = "7")
        int month,

        @Schema(description = "해당 월 포획 마릿수", example = "9")
        long count
) {}
