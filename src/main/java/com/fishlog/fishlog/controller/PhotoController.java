package com.fishlog.fishlog.controller;

import com.fishlog.fishlog.dto.response.FishPhotoResponse;
import com.fishlog.fishlog.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fish-logs/{fishLogId}/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    /** 사진 업로드 + AI 자동 분류 시도 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FishPhotoResponse> upload(
            @PathVariable Long fishLogId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(photoService.uploadPhoto(fishLogId, file));
    }

    /** 특정 낚시 기록의 사진 목록 조회 */
    @GetMapping
    public ResponseEntity<List<FishPhotoResponse>> getPhotos(@PathVariable Long fishLogId) {
        return ResponseEntity.ok(photoService.getPhotos(fishLogId));
    }

    /** 이미 저장된 사진에 대해 AI 분류 (재)시도 */
    @PostMapping("/{photoId}/classify")
    public ResponseEntity<FishPhotoResponse> classify(
            @PathVariable Long fishLogId,
            @PathVariable Long photoId) {
        return ResponseEntity.ok(photoService.classifyPhoto(fishLogId, photoId));
    }

    /** 사진 삭제 */
    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long fishLogId,
            @PathVariable Long photoId) {
        photoService.deletePhoto(fishLogId, photoId);
        return ResponseEntity.noContent().build();
    }
}
