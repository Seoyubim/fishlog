package com.fishlog.fishlog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishlog.fishlog.dto.response.AiClassificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini Vision API를 이용한 어종 분류 구현체.
 *
 * <p>사용 모델: gemini-1.5-flash (무료 티어 제공)
 * <p>API 엔드포인트:
 * POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api-key}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiFishClassificationClient implements AiFishClassificationClient {

    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String model;

    private static final String PROMPT =
            "당신은 어류 전문가입니다. 이 사진 속 물고기의 어종을 반드시 한국어로 식별해 주세요.\n" +
            "규칙:\n" +
            "1. 물고기가 보이면 반드시 어종명을 추측해서 답하세요. 확신이 없어도 가장 가능성 높은 어종을 답하세요.\n" +
            "2. 확신도가 낮더라도(0.3 이상이면) 어종명을 반드시 명시하세요.\n" +
            "3. 사진이 완전히 흐릿하거나, 물고기가 아예 없는 경우에만 fish_name을 \"알 수 없음\"으로 하세요.\n" +
            "4. 응답은 반드시 JSON 형식({ \"fish_name\": \"...\", \"confidence\": ... })으로만 하세요. 다른 텍스트는 절대 포함하지 마세요.\n" +
            "예시: { \"fish_name\": \"광어\", \"confidence\": 0.85 }";

    @Override
    public AiClassificationResult classify(MultipartFile imageFile) {
        try {
            byte[] imageBytes = imageFile.getBytes();
            String mimeType = resolveMimeType(imageFile.getContentType(), imageFile.getOriginalFilename());
            return callGeminiApi(imageBytes, mimeType);
        } catch (IOException e) {
            log.warn("Gemini 분류 실패 - 이미지 읽기 오류: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public AiClassificationResult classifyByResource(Resource imageResource) {
        try {
            byte[] imageBytes = imageResource.getInputStream().readAllBytes();
            String mimeType = resolveMimeType(null, imageResource.getFilename());
            return callGeminiApi(imageBytes, mimeType);
        } catch (IOException e) {
            log.warn("Gemini 분류 실패 - 리소스 읽기 오류: {}", e.getMessage());
            return null;
        }
    }

    // --- private ---

    private AiClassificationResult callGeminiApi(byte[] imageBytes, String mimeType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("text", PROMPT),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", base64Image
                                ))
                        )
                ))
        );

        try {
            String responseJson = geminiRestClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseResponse(responseJson);
        } catch (Exception e) {
            log.warn("Gemini API 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gemini 응답에서 텍스트를 추출하고 AiClassificationResult로 파싱한다.
     *
     * <p>Gemini 응답 구조:
     * <pre>
     * {
     *   "candidates": [{
     *     "content": {
     *       "parts": [{ "text": "{\"fish_name\": \"광어\", \"confidence\": 0.92}" }]
     *     }
     *   }]
     * }
     * </pre>
     */
    private AiClassificationResult parseResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            String text = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();

            log.debug("Gemini 원문 응답: {}", text);
            String jsonText = stripMarkdownCodeBlock(text);
            AiClassificationResult result = objectMapper.readValue(jsonText, AiClassificationResult.class);
            log.info("Gemini 분류 완료: fishName={}, confidence={}", result.fishName(), result.confidence());
            return result;
        } catch (Exception e) {
            log.warn("Gemini 응답 파싱 실패 - 원문: {}, 오류: {}", responseJson, e.getMessage());
            return null;
        }
    }

    /**
     * Gemini가 응답을 ```json ... ``` 형태로 감싸는 경우를 제거한다.
     */
    private String stripMarkdownCodeBlock(String text) {
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }

    /**
     * Content-Type 헤더 또는 파일 확장자로 MIME 타입을 결정한다.
     * 판단 불가 시 image/jpeg를 기본값으로 사용한다.
     */
    private String resolveMimeType(String contentType, String filename) {
        if (contentType != null && contentType.startsWith("image/")) {
            return contentType;
        }
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".png"))  return "image/png";
            if (lower.endsWith(".gif"))  return "image/gif";
            if (lower.endsWith(".webp")) return "image/webp";
        }
        return "image/jpeg";
    }
}
