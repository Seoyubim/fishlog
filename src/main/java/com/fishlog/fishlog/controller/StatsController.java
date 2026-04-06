package com.fishlog.fishlog.controller;

import com.fishlog.fishlog.dto.response.stats.FishingStatsResponse;
import com.fishlog.fishlog.exception.ErrorResponse;
import com.fishlog.fishlog.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "낚시 통계", description = "개인 낚시 활동 연간 통계 리포트 API")
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(
            summary = "연간 낚시 통계 조회",
            description = """
                    로그인한 사용자의 연간 낚시 통계 리포트를 조회합니다.

                    **포함 정보:**
                    - 연간 총 포획 마릿수
                    - 최대/평균 어체장 (cm)
                    - 월별 포획 마릿수 추이
                    - 어종별 포획 횟수 순위 (AI 분류명 우선)
                    - 수계(민물/바다)별 통계
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/annual")
    public ResponseEntity<FishingStatsResponse> getAnnualStats(
            @Parameter(description = "집계 연도 (미입력 시 현재 연도)", example = "2025")
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(statsService.getAnnualStats(year));
    }
}
