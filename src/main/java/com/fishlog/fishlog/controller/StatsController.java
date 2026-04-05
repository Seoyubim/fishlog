package com.fishlog.fishlog.controller;

import com.fishlog.fishlog.dto.response.stats.FishingStatsResponse;
import com.fishlog.fishlog.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 개인 연간 낚시 통계 리포트 조회
     *
     * @param userId 사용자 ID
     * @param year   집계 연도 (선택, 기본값: 현재 연도)
     */
    @GetMapping("/annual")
    public ResponseEntity<FishingStatsResponse> getAnnualStats(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(statsService.getAnnualStats(userId, year));
    }
}
