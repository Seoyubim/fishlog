package com.fishlog.fishlog.service;

import com.fishlog.fishlog.config.StorageConfig;
import com.fishlog.fishlog.domain.FishLog;
import com.fishlog.fishlog.domain.FishPhoto;
import com.fishlog.fishlog.dto.response.AiClassificationResult;
import com.fishlog.fishlog.dto.response.FishPhotoResponse;
import com.fishlog.fishlog.exception.FishLogNotFoundException;
import com.fishlog.fishlog.exception.PhotoNotFoundException;
import com.fishlog.fishlog.repository.FishLogRepository;
import com.fishlog.fishlog.repository.FishPhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoService {

    private final FishLogRepository fishLogRepository;
    private final FishPhotoRepository fishPhotoRepository;
    private final StorageService storageService;
    private final AiFishClassificationClient aiClient;
    private final StorageConfig storageConfig;

    /**
     * 이미지를 업로드하고, 성공 시 AI 어종 분류를 비동기 없이 즉시 시도한다.
     * AI 분류 실패는 업로드 성공에 영향을 주지 않는다.
     */
    @Transactional
    public FishPhotoResponse uploadPhoto(Long fishLogId, MultipartFile file) {
        FishLog fishLog = fishLogRepository.findById(fishLogId)
                .orElseThrow(() -> new FishLogNotFoundException(fishLogId));

        // 1. 파일 저장
        String photoUrl = storageService.store(file);

        // 2. FishPhoto 엔티티 생성
        FishPhoto photo = FishPhoto.builder()
                .fishLog(fishLog)
                .photoUrl(photoUrl)
                .build();
        fishPhotoRepository.save(photo);

        // 3. AI 분류 시도 (실패해도 업로드는 성공)
        tryAiClassification(fishLog, photo, file);

        return FishPhotoResponse.from(photo);
    }

    /**
     * 이미 저장된 사진에 대해 AI 분류를 (재)시도한다.
     */
    @Transactional
    public FishPhotoResponse classifyPhoto(Long fishLogId, Long photoId) {
        FishPhoto photo = fishPhotoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId));

        FishLog fishLog = photo.getFishLog();
        if (!fishLog.getId().equals(fishLogId)) {
            throw new PhotoNotFoundException(photoId);
        }

        String filename = Paths.get(photo.getPhotoUrl()).getFileName().toString();
        String filePath = Paths.get(storageConfig.getUploadDir()).resolve(filename).toString();
        Resource imageResource = new FileSystemResource(filePath);

        AiClassificationResult result = aiClient.classifyByResource(imageResource);
        applyClassificationResult(fishLog, photo, result);

        return FishPhotoResponse.from(photo);
    }

    public List<FishPhotoResponse> getPhotos(Long fishLogId) {
        return fishPhotoRepository.findAllByFishLogId(fishLogId).stream()
                .map(FishPhotoResponse::from)
                .toList();
    }

    @Transactional
    public void deletePhoto(Long fishLogId, Long photoId) {
        FishPhoto photo = fishPhotoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId));

        if (!photo.getFishLog().getId().equals(fishLogId)) {
            throw new PhotoNotFoundException(photoId);
        }

        storageService.delete(photo.getPhotoUrl());
        fishPhotoRepository.delete(photo);
    }

    // --- private helpers ---

    private void tryAiClassification(FishLog fishLog, FishPhoto photo, MultipartFile file) {
        try {
            AiClassificationResult result = aiClient.classify(file);
            applyClassificationResult(fishLog, photo, result);
        } catch (Exception e) {
            log.warn("AI 분류 중 예외 발생 (무시): photoId={}, error={}", photo.getId(), e.getMessage());
        }
    }

    private void applyClassificationResult(FishLog fishLog, FishPhoto photo, AiClassificationResult result) {
        if (result == null) {
            return;
        }
        photo.applyAiResult(result.confidence());
        fishLog.updateAiResult(result.fishName());
        log.info("AI 분류 적용: fishLogId={}, fishNameAi={}, confidence={}",
                fishLog.getId(), result.fishName(), result.confidence());
    }
}
