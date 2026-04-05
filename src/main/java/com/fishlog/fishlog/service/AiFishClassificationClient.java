package com.fishlog.fishlog.service;

import com.fishlog.fishlog.dto.response.AiClassificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * AI 어종 분류 외부 API와 통신하는 클라이언트.
 *
 * <p>AI 서버 API 스펙 (예정):
 * <pre>
 * POST /predict
 * Content-Type: multipart/form-data
 * Body: file (image file)
 *
 * Response 200:
 * {
 *   "fish_name": "광어",
 *   "confidence": 0.92
 * }
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFishClassificationClient {

    private final RestClient aiRestClient;

    /**
     * 이미지 파일을 AI 서버에 전송하여 어종 분류 결과를 받는다.
     *
     * @param imageFile 분류할 이미지 파일
     * @return AI 분류 결과 (어종명, 신뢰도). 분류 실패 시 null 반환.
     */
    public AiClassificationResult classify(MultipartFile imageFile) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", imageFile.getResource());

            AiClassificationResult result = aiRestClient.post()
                    .uri("/predict")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(AiClassificationResult.class);

            log.info("AI 분류 완료: fishName={}, confidence={}", result.fishName(), result.confidence());
            return result;

        } catch (RestClientException e) {
            log.warn("AI 분류 API 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 저장된 이미지 경로로 AI 분류를 요청한다.
     * StorageService에 파일이 저장된 후 photoUrl로 재요청할 때 사용.
     *
     * @param imageResource 이미지 Resource 객체
     * @return AI 분류 결과. 분류 실패 시 null 반환.
     */
    public AiClassificationResult classifyByResource(Resource imageResource) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", imageResource);

            AiClassificationResult result = aiRestClient.post()
                    .uri("/predict")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(AiClassificationResult.class);

            log.info("AI 분류 완료: fishName={}, confidence={}", result.fishName(), result.confidence());
            return result;

        } catch (RestClientException e) {
            log.warn("AI 분류 API 호출 실패 (Resource): {}", e.getMessage());
            return null;
        }
    }
}
