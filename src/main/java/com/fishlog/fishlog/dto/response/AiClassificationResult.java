package com.fishlog.fishlog.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 어종 분류 결과 (Gemini Vision API 응답)")
public record AiClassificationResult(

        @Schema(description = "AI가 판별한 어종명 (한국어)", example = "광어")
        @JsonProperty("fish_name") String fishName,

        @Schema(description = "분류 신뢰도 (0.0 ~ 1.0)", example = "0.92")
        @JsonProperty("confidence") Double confidence
) {}
