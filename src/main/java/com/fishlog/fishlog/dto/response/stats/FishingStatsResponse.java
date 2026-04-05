package com.fishlog.fishlog.dto.response.stats;

import java.util.List;

/**
 * 개인 낚시 통계 리포트 응답
 */
public record FishingStatsResponse(
        Long userId,
        int year,

        /** 연간 총 마릿수 */
        long totalCount,

        /** 최대 어체장 (cm), 기록 없으면 null */
        Double maxLengthCm,

        /** 평균 어체장 (cm), 기록 없으면 null */
        Double avgLengthCm,

        /** 월별 포획 마릿수 (데이터가 없는 달은 포함되지 않음) */
        List<MonthlyCountDto> monthlyCounts,

        /** 어종별 포획 횟수 순위 (AI 분류명 우선, 없으면 사용자 입력명) */
        List<FishRankDto> fishRanking,

        /** 수계(민물/바다)별 포획 횟수 */
        List<WaterTypeStatDto> waterTypeStats
) {}
