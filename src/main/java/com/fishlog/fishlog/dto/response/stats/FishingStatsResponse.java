package com.fishlog.fishlog.dto.response.stats;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "개인 낚시 통계 리포트 응답")
public record FishingStatsResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "집계 연도", example = "2025")
        int year,

        @Schema(description = "연간 총 포획 마릿수", example = "47")
        long totalCount,

        @Schema(description = "최대 어체장 (cm), 기록 없으면 null", example = "68.5")
        Double maxLengthCm,

        @Schema(description = "평균 어체장 (cm), 기록 없으면 null", example = "34.2")
        Double avgLengthCm,

        @Schema(description = "월별 포획 마릿수 (데이터가 있는 달만 포함)")
        List<MonthlyCountDto> monthlyCounts,

        @Schema(description = "어종별 포획 횟수 순위 (AI 분류명 우선, 없으면 사용자 입력명)")
        List<FishRankDto> fishRanking,

        @Schema(description = "수계(민물/바다)별 포획 횟수")
        List<WaterTypeStatDto> waterTypeStats
) {}
