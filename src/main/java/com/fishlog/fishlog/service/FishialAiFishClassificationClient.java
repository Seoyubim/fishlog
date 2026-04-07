package com.fishlog.fishlog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishlog.fishlog.dto.response.AiClassificationResult;
import com.fishlog.fishlog.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Fishial.AI REST API를 이용한 어종 분류 구현체.
 *
 * <p><b>인증 흐름:</b>
 * <pre>
 * POST https://api-recognition.fishial.ai/v2/auth
 * Body : { "client_id": "...", "client_secret": "..." }
 * Response: { "token_type": "Bearer", "access_token": "..." }
 * </pre>
 * 토큰 유효시간 ~10분. 만료 30초 전 자동 갱신.
 *
 * <p><b>인식 흐름:</b>
 * <pre>
 * POST https://api-recognition.fishial.ai/v2/recognize
 * Header : Authorization: Bearer {access_token}
 * Content-Type: application/octet-stream
 * Body : 이미지 raw binary (최대 20MB)
 * </pre>
 *
 * <p><b>인식 API 실제 응답 구조:</b>
 * <pre>{@code
 * {
 *   "ok": true,
 *   "queryToken": "...",
 *   "objects": [                          // 감지된 물고기 영역 목록
 *     {
 *       "bbox": [...],
 *       "species": [                      // 후보 어종 (certainty 내림차순)
 *         { "id": "3144bfc5-...", "certainty": 0.53 },
 *         { "id": "17edd9f7-...", "certainty": 0.45 }
 *       ]
 *     }
 *   ],
 *   "definitions": {                      // id → 어종 정보 매핑
 *     "3144bfc5-...": {
 *       "commonName":    "Lake sturgeon",
 *       "scientificName": "Acipenser fulvescens",
 *       "imageUrl":      "https://..."
 *     }
 *   }
 * }
 * }</pre>
 * 파싱 경로: {@code objects[0].species[0].id} → {@code definitions[id].commonName}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FishialAiFishClassificationClient implements AiFishClassificationClient {

    private final RestClient fishialRestClient;
    private final ObjectMapper objectMapper;
    private final GeminiTranslationService translationService;

    @Value("${ai.fishial.client-id}")
    private String clientId;

    @Value("${ai.fishial.client-secret}")
    private String clientSecret;

    /** Access Token 캐시 */
    private volatile String cachedToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    private static final String TOKEN_PATH     = "/v2/auth";
    private static final String RECOGNIZE_PATH = "/v2/recognize";

    /** Fishial.AI 토큰 유효시간 (문서상 ~10분, 30초 여유 뺀 570초) */
    private static final long TOKEN_TTL_SECONDS = 570;

    // ------------------------------------------------------------------ public API

    @Override
    public AiClassificationResult classify(MultipartFile imageFile) {
        try {
            String filename = imageFile.getOriginalFilename() != null
                    ? imageFile.getOriginalFilename() : "image.jpg";
            return callRecognitionApi(imageFile.getBytes(), filename);
        } catch (AiServiceException e) {
            throw e;
        } catch (IOException e) {
            log.warn("Fishial 분류 실패 - 이미지 읽기 오류: {}", e.getMessage());
            throw new AiServiceException("이미지 파일 읽기에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    public AiClassificationResult classifyByResource(Resource imageResource) {
        try {
            String filename = imageResource.getFilename() != null
                    ? imageResource.getFilename() : "image.jpg";
            return callRecognitionApi(imageResource.getInputStream().readAllBytes(), filename);
        } catch (AiServiceException e) {
            throw e;
        } catch (IOException e) {
            log.warn("Fishial 분류 실패 - 리소스 읽기 오류: {}", e.getMessage());
            throw new AiServiceException("이미지 리소스 읽기에 실패했습니다: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ private

    /**
     * Fishial.AI 인식 API 호출.
     * Content-Type: application/octet-stream (raw binary — multipart 아님)
     * Fishial-Image-Filename 헤더: 선택적이지만 인식률 향상에 도움.
     */
    private AiClassificationResult callRecognitionApi(byte[] imageBytes, String filename) {
        String token = getAccessToken();

        String responseJson;
        try {
            responseJson = fishialRestClient.post()
                    .uri(RECOGNIZE_PATH)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("Fishial-Image-Filename", toAsciiFilename(filename))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(imageBytes)
                    .retrieve()
                    .onStatus(status -> status.value() == 401, (req, resp) -> {
                        invalidateToken();
                        throw new AiServiceException(
                                "Fishial.AI 인증 실패(401): 토큰이 만료되었거나 자격증명이 잘못되었습니다. " +
                                "ai.fishial.client-id / client-secret 설정을 확인하세요.", 401);
                    })
                    .onStatus(status -> status.value() == 403, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 크레딧 부족(403): portal.fishial.ai 대시보드에서 " +
                                "플랜 또는 크레딧을 확인하세요.", 403);
                    })
                    .onStatus(status -> status.value() == 415, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 지원하지 않는 이미지 형식(415): JPEG, PNG, WEBP, GIF, HEIC 등을 사용하세요.", 415);
                    })
                    .onStatus(status -> status.value() == 429, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 요청 한도 초과(429): 분당 100회 제한. 잠시 후 다시 시도해 주세요.", 429);
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI API 요청 오류: HTTP " + resp.getStatusCode().value(), resp.getStatusCode().value());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 서버 오류(5xx): 잠시 후 다시 시도해 주세요. HTTP " + resp.getStatusCode().value(),
                                resp.getStatusCode().value());
                    })
                    .body(String.class);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Fishial.AI 인식 API 호출 중 예외: {}", e.getMessage(), e);
            throw new AiServiceException("Fishial.AI API 호출 실패: " + e.getMessage());
        }

        return parseRecognitionResponse(responseJson);
    }

    /**
     * Fishial.AI Access Token 발급 (캐시 우선).
     *
     * <p>응답: {@code { "token_type": "Bearer", "access_token": "..." }}
     * expires_in 필드 없음 → TTL {@value TOKEN_TTL_SECONDS}초 고정.
     */
    private synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }

        log.info("Fishial.AI Access Token 갱신 중...");
        Map<String, String> body = Map.of(
                "client_id",     clientId,
                "client_secret", clientSecret
        );

        String tokenResponse;
        try {
            tokenResponse = fishialRestClient.post()
                    .uri(TOKEN_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(status -> status.value() == 401 || status.value() == 403, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 인증 실패: client-id 또는 client-secret이 올바르지 않습니다. " +
                                "ai.fishial.client-id / ai.fishial.client-secret 설정을 확인하세요.",
                                resp.getStatusCode().value());
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 토큰 요청 실패: HTTP " + resp.getStatusCode().value(), resp.getStatusCode().value());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 인증 서버 오류: 잠시 후 다시 시도해 주세요.", resp.getStatusCode().value());
                    })
                    .body(String.class);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Fishial.AI 토큰 요청 중 예외: {}", e.getMessage(), e);
            throw new AiServiceException("Fishial.AI 토큰 획득 실패: " + e.getMessage());
        }

        return parseAndCacheToken(tokenResponse);
    }

    private String parseAndCacheToken(String tokenResponse) {
        try {
            JsonNode json  = objectMapper.readTree(tokenResponse);
            String   token = json.path("access_token").asText(null);

            if (token == null || token.isBlank()) {
                log.error("Fishial.AI 토큰 응답에 access_token 필드 없음. 원문: {}", tokenResponse);
                throw new AiServiceException(
                        "Fishial.AI 토큰 응답 형식이 예상과 다릅니다. 원문: " + tokenResponse);
            }

            cachedToken    = token;
            tokenExpiresAt = Instant.now().plusSeconds(TOKEN_TTL_SECONDS);
            log.info("Fishial.AI Access Token 갱신 완료. 캐시 TTL: {}초", TOKEN_TTL_SECONDS);
            return cachedToken;

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Fishial.AI 토큰 응답 파싱 실패: {}, 원문: {}", e.getMessage(), tokenResponse);
            throw new AiServiceException("Fishial.AI 토큰 응답 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * 인식 API 응답 파싱.
     *
     * <p>실제 구조:
     * <ul>
     *   <li>{@code objects[0].species[0].id}        → 어종 UUID</li>
     *   <li>{@code objects[0].species[0].certainty} → 신뢰도 (0~1)</li>
     *   <li>{@code definitions[id].commonName}      → 영문 어종명</li>
     * </ul>
     */
    private AiClassificationResult parseRecognitionResponse(String responseJson) {
        try {
            JsonNode root    = objectMapper.readTree(responseJson);
            JsonNode objects = root.path("objects");

            if (objects.isMissingNode() || objects.isEmpty()) {
                log.info("Fishial.AI 인식 결과 없음 — 이미지에서 물고기를 감지하지 못함 (objects 배열 비어있음).");
                return new AiClassificationResult("알 수 없음", 0.0);
            }

            JsonNode species = objects.get(0).path("species");
            if (species.isMissingNode() || species.isEmpty()) {
                log.info("Fishial.AI 인식 결과 없음 — 물고기는 감지됐으나 어종 후보가 없음 (species 배열 비어있음).");
                return new AiClassificationResult("알 수 없음", 0.0);
            }

            // objects[0].species[0]: 가장 높은 certainty의 후보
            JsonNode topSpecies = species.get(0);
            String   speciesId  = topSpecies.path("id").asText(null);
            double   certainty  = topSpecies.path("certainty").asDouble(0.0);

            if (speciesId == null || speciesId.isBlank()) {
                log.warn("Fishial.AI species[0].id 필드 없음. species[0]: {}", topSpecies);
                return new AiClassificationResult("알 수 없음", 0.0);
            }

            // definitions[speciesId].commonName 으로 어종명 조회
            JsonNode definition = root.path("definitions").path(speciesId);
            String englishName    = definition.path("commonName").asText("알 수 없음");
            String scientificName = definition.path("scientificName").asText(null);
            String fishName       = translationService.translateFishName(englishName, scientificName);

            log.info("Fishial.AI 분류 완료: {} ({}) → {}, certainty={}", englishName, scientificName, fishName, certainty);
            return new AiClassificationResult(fishName, certainty);

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Fishial.AI 응답 파싱 실패: {}, 원문: {}", e.getMessage(), responseJson);
            throw new AiServiceException("Fishial.AI 응답 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * HTTP 헤더에 안전하게 담을 수 있도록 파일명을 ASCII 전용으로 변환한다.
     * 한글 등 비ASCII 문자가 포함된 경우 확장자만 보존하고 "image.{ext}" 형태로 대체한다.
     */
    private String toAsciiFilename(String filename) {
        if (filename == null || filename.isBlank()) return "image.jpg";
        boolean isAscii = filename.chars().allMatch(c -> c < 128);
        if (isAscii) return filename;

        // 확장자 추출 후 ASCII 안전 이름으로 대체
        int dotIndex = filename.lastIndexOf('.');
        String ext = dotIndex >= 0 ? filename.substring(dotIndex + 1).toLowerCase() : "jpg";
        return "image." + ext;
    }

    private synchronized void invalidateToken() {
        cachedToken    = null;
        tokenExpiresAt = Instant.EPOCH;
    }
}
