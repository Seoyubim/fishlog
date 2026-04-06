package com.fishlog.fishlog.dto.response.stats;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "어종별 포획 횟수")
public record FishRankDto(

        @Schema(description = "어종명", example = "광어")
        String fishName,

        @Schema(description = "포획 횟수", example = "12")
        long count
) {}
