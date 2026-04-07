package com.fishlog.fishlog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Google Gemini 2.5 Flash를 사용해 영문 어종명을 한국어로 번역하는 서비스.
 *
 * <p>Fishial.AI가 반환하는 영어 common name (예: "Southern puffer")을
 * 한국어 일반명 (예: "복어")으로 변환한다.
 *
 * <p>동일 어종의 반복 API 호출을 막기 위해 인메모리 캐시를 사용한다.
 * 번역 실패 시 영어 원문을 그대로 반환하여 분류 결과를 항상 보장한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiTranslationService {

    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String model;

    /** 어종명 번역 캐시: "Southern puffer" → "복어" */
    private final Map<String, String> translationCache = new ConcurrentHashMap<>();

    /**
     * 영어 물고기 이름을 한국어로 번역한다.
     *
     * @param englishName    Fishial.AI commonName (예: "Southern puffer")
     * @param scientificName Fishial.AI scientificName (예: "Sphoeroides nephelus"). 없으면 null.
     * @return 한국어 어종명. 번역 실패 시 영어 원문 반환.
     */
    public String translateFishName(String englishName, String scientificName) {
        if (englishName == null || englishName.isBlank() || englishName.equals("알 수 없음")) {
            return englishName;
        }
        String cacheKey = englishName + "|" + (scientificName != null ? scientificName : "");
        return translationCache.computeIfAbsent(cacheKey, k -> callGemini(englishName, scientificName));
    }

    // ------------------------------------------------------------------ private

    private String callGemini(String englishName, String scientificName) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 물고기의 한국어 일반명을 알려줘.\n");
        prompt.append("영어 이름: ").append(englishName).append("\n");
        if (scientificName != null && !scientificName.isBlank()) {
            prompt.append("학명: ").append(scientificName).append("\n");
        }
        prompt.append("\n규칙:\n");
        prompt.append("1. 정확한 한국어 일반명이 있으면 그것만 답해. (예: 복어, 광어, 농어)\n");
        prompt.append("2. 정확한 이름이 없으면 같은 속(genus)이나 과(family)의 한국 어종명에 '류'를 붙여서 답해. (예: 감성돔류, 도미류)\n");
        prompt.append("3. 한국어 이름만 답하고 다른 텍스트나 설명은 절대 포함하지 마.");

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt.toString()))
                ))
        );

        try {
            String responseJson = geminiRestClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);
            String koreanName = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText(null);

            if (koreanName == null || koreanName.isBlank()) {
                log.warn("Gemini 번역 응답이 비어있음. 영어 이름 유지: {}", englishName);
                return englishName;
            }

            String result = koreanName.trim();
            log.info("Gemini 번역 완료: {} → {}", englishName, result);
            return result;

        } catch (Exception e) {
            log.warn("Gemini 번역 실패. 영어 이름 유지: {} ({}), error: {}", englishName, scientificName, e.getMessage());
            return englishName;
        }
    }
}
