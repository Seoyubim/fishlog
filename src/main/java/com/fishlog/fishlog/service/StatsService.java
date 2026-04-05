package com.fishlog.fishlog.service;

import com.fishlog.fishlog.domain.enums.WaterType;
import com.fishlog.fishlog.dto.response.stats.FishRankDto;
import com.fishlog.fishlog.dto.response.stats.FishingStatsResponse;
import com.fishlog.fishlog.dto.response.stats.MonthlyCountDto;
import com.fishlog.fishlog.dto.response.stats.WaterTypeStatDto;
import com.fishlog.fishlog.exception.UserNotFoundException;
import com.fishlog.fishlog.repository.FishLogRepository;
import com.fishlog.fishlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final FishLogRepository fishLogRepository;
    private final UserRepository userRepository;

    /**
     * 특정 사용자의 연간 낚시 통계를 집계해 반환한다.
     *
     * @param userId 대상 사용자 ID
     * @param year   집계 연도 (null이면 현재 연도)
     */
    public FishingStatsResponse getAnnualStats(Long userId, Integer year) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        long totalCount = fishLogRepository.countByUserIdAndYear(userId, targetYear);
        Double maxLengthCm = fishLogRepository.maxLengthByUserIdAndYear(userId, targetYear);
        Double avgLengthCm = roundScale2(fishLogRepository.avgLengthByUserIdAndYear(userId, targetYear));

        List<MonthlyCountDto> monthlyCounts = fishLogRepository
                .countByMonth(userId, targetYear)
                .stream()
                .map(row -> new MonthlyCountDto(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).longValue()))
                .toList();

        List<FishRankDto> fishRanking = fishLogRepository
                .fishRankByUserId(userId)
                .stream()
                .map(row -> new FishRankDto(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .toList();

        List<WaterTypeStatDto> waterTypeStats = fishLogRepository
                .waterTypeStatsByUserId(userId)
                .stream()
                .map(row -> new WaterTypeStatDto(
                        (WaterType) row[0],
                        ((Number) row[1]).longValue()))
                .toList();

        return new FishingStatsResponse(
                userId,
                targetYear,
                totalCount,
                maxLengthCm,
                avgLengthCm,
                monthlyCounts,
                fishRanking,
                waterTypeStats
        );
    }

    private Double roundScale2(Double value) {
        if (value == null) return null;
        return Math.round(value * 100.0) / 100.0;
    }
}
