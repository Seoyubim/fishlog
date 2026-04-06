package com.fishlog.fishlog.controller;

import com.fishlog.fishlog.dto.request.FishLogCreateRequest;
import com.fishlog.fishlog.dto.request.FishLogUpdateRequest;
import com.fishlog.fishlog.dto.response.FishLogDetailResponse;
import com.fishlog.fishlog.dto.response.FishLogResponse;
import com.fishlog.fishlog.exception.ErrorResponse;
import com.fishlog.fishlog.service.FishLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "낚시 기록", description = "낚시 기록 생성·조회·수정·삭제 API")
@RestController
@RequestMapping("/api/v1/fish-logs")
@RequiredArgsConstructor
public class FishLogController {

    private final FishLogService fishLogService;

    @Operation(summary = "낚시 기록 생성", description = "새로운 낚시 기록을 생성합니다. 사진은 별도 업로드 API를 사용하세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 오류 (필수 항목 누락 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "userId에 해당하는 사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<FishLogDetailResponse> create(
            @Valid @RequestBody FishLogCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fishLogService.create(request));
    }

    @Operation(summary = "낚시 기록 목록 조회", description = "특정 사용자의 낚시 기록 목록을 페이지 단위로 조회합니다. 기본 정렬: 최신순")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "userId에 해당하는 사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<FishLogResponse>> getList(
            @Parameter(description = "사용자 ID", example = "1", required = true)
            @RequestParam Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(fishLogService.getList(userId, pageable));
    }

    @Operation(summary = "낚시 기록 상세 조회", description = "낚시 기록 ID로 상세 정보(사진 목록 포함)를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 낚시 기록 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<FishLogDetailResponse> getDetail(
            @Parameter(description = "낚시 기록 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(fishLogService.getDetail(id));
    }

    @Operation(summary = "낚시 기록 수정", description = "낚시 기록의 내용을 수정합니다. 모든 필드를 전송해야 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 낚시 기록 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<FishLogDetailResponse> update(
            @Parameter(description = "낚시 기록 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody FishLogUpdateRequest request) {
        return ResponseEntity.ok(fishLogService.update(id, request));
    }

    @Operation(summary = "낚시 기록 삭제", description = "낚시 기록과 연관된 사진을 모두 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 낚시 기록 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "낚시 기록 ID", example = "1") @PathVariable Long id) {
        fishLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
