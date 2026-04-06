package com.fishlog.fishlog.service;

import com.fishlog.fishlog.dto.response.AiClassificationResult;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI 어종 분류 클라이언트 인터페이스.
 * 현재 구현체: {@link GeminiAiFishClassificationClient}
 */
public interface AiFishClassificationClient {

    /**
     * 업로드된 이미지 파일을 AI에 전송해 어종을 분류한다.
     *
     * @param imageFile 분류할 이미지 파일
     * @return AI 분류 결과 (어종명, 신뢰도). 분류 실패 시 null 반환.
     */
    AiClassificationResult classify(MultipartFile imageFile);

    /**
     * 저장된 이미지 Resource로 AI 분류를 (재)시도한다.
     *
     * @param imageResource 이미지 Resource 객체
     * @return AI 분류 결과. 분류 실패 시 null 반환.
     */
    AiClassificationResult classifyByResource(Resource imageResource);
}
