package com.fishlog.fishlog.controller;

import com.fishlog.fishlog.dto.response.FishPhotoResponse;
import com.fishlog.fishlog.exception.ErrorResponse;
import com.fishlog.fishlog.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "사진 & AI 분류", description = "낚시 사진 업로드 및 Gemini AI 어종 분류 API")
@RestController
@RequestMapping("/api/v1/fish-logs/{fishLogId}/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @Operation(
            summary = "사진 업로드 + AI 어종 자동 분류",
            description = """
                    사진을 업로드하고 Google Gemini Vision API로 어종을 자동 분류합니다.

                    - 지원 형식: JPEG, PNG, GIF, WEBP (최대 10MB)
                    - AI 분류 실패 시에도 사진 업로드는 성공으로 처리됩니다.
                    - 분류 성공 시 낚시 기록의 `fishNameAi` 필드가 자동으로 업데이트됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 성공 (AI 분류 결과 포함 가능)"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 낚시 기록 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "파일 형식 오류 또는 용량 초과",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FishPhotoResponse> upload(
            @Parameter(description = "낚시 기록 ID", example = "1") @PathVariable Long fishLogId,
            @Parameter(description = "업로드할 이미지 파일 (JPEG/PNG/GIF/WEBP, 최대 10MB)")
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(photoService.uploadPhoto(fishLogId, file));
    }

    @Operation(summary = "사진 목록 조회", description = "특정 낚시 기록에 첨부된 사진 목록과 AI 분류 결과를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 낚시 기록 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<FishPhotoResponse>> getPhotos(
            @Parameter(description = "낚시 기록 ID", example = "1") @PathVariable Long fishLogId) {
        return ResponseEntity.ok(photoService.getPhotos(fishLogId));
    }

    @Operation(
            summary = "AI 어종 재분류",
            description = "이미 저장된 사진에 대해 Gemini AI 어종 분류를 다시 시도합니다. AI가 처음에 실패했을 때 사용하세요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재분류 성공 (분류 실패 시에도 200 반환, aiClassified 필드로 확인)"),
            @ApiResponse(responseCode = "404", description = "해당 사진 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{photoId}/classify")
    public ResponseEntity<FishPhotoResponse> classify(
            @Parameter(description = "낚시 기록 ID", example = "1") @PathVariable Long fishLogId,
            @Parameter(description = "사진 ID", example = "1") @PathVariable Long photoId) {
        return ResponseEntity.ok(photoService.classifyPhoto(fishLogId, photoId));
    }

    @Operation(summary = "사진 삭제", description = "사진 파일과 DB 레코드를 함께 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사진 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "낚시 기록 ID", example = "1") @PathVariable Long fishLogId,
            @Parameter(description = "사진 ID", example = "1") @PathVariable Long photoId) {
        photoService.deletePhoto(fishLogId, photoId);
        return ResponseEntity.noContent().build();
    }
}
