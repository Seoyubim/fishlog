package com.fishlog.fishlog.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AI 어종 분류 API 응답 DTO
 * AI 서버 응답 예시: { "fish_name": "광어", "confidence": 0.92 }
 */
public record AiClassificationResult(
        @JsonProperty("fish_name") String fishName,
        @JsonProperty("confidence") Double confidence
) {}
