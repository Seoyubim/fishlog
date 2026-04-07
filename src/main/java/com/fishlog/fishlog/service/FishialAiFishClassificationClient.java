package com.fishlog.fishlog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishlog.fishlog.dto.response.AiClassificationResult;
import com.fishlog.fishlog.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Fishial.AI REST API를 이용한 어종 분류 구현체.
 *
 * <p><b>인증 흐름 (OAuth2 Client Credentials):</b>
 * <ol>
 *   <li>{@code POST /v1/auth/token} — client_id / client_secret으로 Bearer 토큰 발급</li>
 *   <li>토큰은 만료 60초 전까지 메모리에 캐싱하여 재사용</li>
 * </ol>
 *
 * <p><b>인식 흐름:</b>
 * <ol>
 *   <li>{@code POST /v1/recognition/image} — multipart/form-data로 이미지 전송</li>
 *   <li>응답 {@code results[0]} 에서 어종명과 confidence 추출</li>
 * </ol>
 *
 * <p><b>인식 API 응답 구조:</b>
 * <pre>{@code
 * {
 *   "results": [                         // 감지된 물고기 영역 목록
 *     {
 *       "species": [                     // 후보 어종 목록 (confidence 내림차순)
 *         {
 *           "name":        "Micropterus salmoides",
 *           "common_name": "Largemouth Bass",
 *           "confidence":  0.985
 *         }
 *       ],
 *       "fish_box": [100, 200, 300, 400] // 바운딩 박스 (사용 안 함)
 *     }
 *   ]
 * }
 * }</pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FishialAiFishClassificationClient implements AiFishClassificationClient {

    private final RestClient fishialRestClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.fishial.client-id}")
    private String clientId;

    @Value("${ai.fishial.client-secret}")
    private String clientSecret;

    /** Access Token 캐시 (volatile — 복수 스레드에서 가시성 보장) */
    private volatile String cachedToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    private static final String TOKEN_PATH      = "/v1/auth/token";
    private static final String RECOGNIZE_PATH  = "/v1/recognition/image";

    // ------------------------------------------------------------------ public API

    @Override
    public AiClassificationResult classify(MultipartFile imageFile) {
        try {
            byte[] bytes    = imageFile.getBytes();
            String filename = imageFile.getOriginalFilename() != null
                    ? imageFile.getOriginalFilename() : "image.jpg";
            return callRecognitionApi(bytes, filename);
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
            byte[] bytes    = imageResource.getInputStream().readAllBytes();
            String filename = imageResource.getFilename() != null
                    ? imageResource.getFilename() : "image.jpg";
            return callRecognitionApi(bytes, filename);
        } catch (AiServiceException e) {
            throw e;
        } catch (IOException e) {
            log.warn("Fishial 분류 실패 - 리소스 읽기 오류: {}", e.getMessage());
            throw new AiServiceException("이미지 리소스 읽기에 실패했습니다: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ private

    /**
     * Fishial.AI 인식 API를 호출하고 결과를 반환한다.
     *
     * <p>HTTP 4xx/5xx 응답은 각각의 원인을 명시한 {@link AiServiceException}으로 변환된다.
     */
    private AiClassificationResult callRecognitionApi(byte[] imageBytes, String filename) {
        String token = getAccessToken();

        // Spring RestClient는 ByteArrayResource의 getFilename()을 Content-Disposition에 사용한다.
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() { return filename; }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", imageResource);

        String responseJson;
        try {
            responseJson = fishialRestClient.post()
                    .uri(RECOGNIZE_PATH)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .onStatus(status -> status.value() == 401, (req, resp) -> {
                        // 토큰 만료 가능성 — 캐시 무효화 후 재시도는 여기서 하지 않고 운영자에게 알림
                        invalidateToken();
                        throw new AiServiceException(
                                "Fishial.AI 인증 실패(401): 토큰이 만료되었거나 client-id/secret이 잘못되었습니다. " +
                                "application.yaml의 ai.fishial.* 설정을 확인하세요.", 401);
                    })
                    .onStatus(status -> status.value() == 402, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 크레딧 부족(402): Fishial.AI 대시보드에서 플랜 또는 크레딧을 확인하세요.", 402);
                    })
                    .onStatus(status -> status.value() == 429, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI API 요청 한도 초과(429): Rate Limit에 도달했습니다. 잠시 후 다시 시도해 주세요.", 429);
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
            log.error("Fishial.AI 인식 API 호출 중 예상치 못한 예외: {}", e.getMessage(), e);
            throw new AiServiceException("Fishial.AI API 호출 실패: " + e.getMessage());
        }

        return parseRecognitionResponse(responseJson);
    }

    /**
     * Fishial.AI Access Token을 발급받는다. 유효한 캐시가 있으면 재사용한다.
     *
     * <p>토큰 발급 API:
     * <pre>POST /v1/auth/token
     * Body : { "client_id": "...", "client_secret": "..." }
     * Response: { "token": "...", "expires_in": 86400 }
     *        또는 { "access_token": "...", "expires_in": 3600 }</pre>
     */
    private synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }

        log.info("Fishial.AI Access Token 갱신 중...");
        Map<String, String> tokenRequest = Map.of(
                "client_id",     clientId,
                "client_secret", clientSecret
        );

        String tokenResponse;
        try {
            tokenResponse = fishialRestClient.post()
                    .uri(TOKEN_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(tokenRequest)
                    .retrieve()
                    .onStatus(status -> status.value() == 401 || status.value() == 403, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 인증 실패: client-id 또는 client-secret이 올바르지 않습니다. " +
                                "application.yaml의 ai.fishial.client-id / ai.fishial.client-secret을 확인하세요.",
                                resp.getStatusCode().value());
                    })
                    .onStatus(status -> status.value() == 402, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 계정 크레딧이 부족하거나 구독이 만료되었습니다(402). Fishial.AI 대시보드를 확인하세요.", 402);
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 토큰 요청 실패: HTTP " + resp.getStatusCode().value(), resp.getStatusCode().value());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        throw new AiServiceException(
                                "Fishial.AI 인증 서버 오류(5xx): 잠시 후 다시 시도해 주세요.", resp.getStatusCode().value());
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
            JsonNode json = objectMapper.readTree(tokenResponse);

            // 응답 필드명이 "token" 또는 "access_token" 두 가지 가능
            String token = json.has("token")
                    ? json.path("token").asText(null)
                    : json.path("access_token").asText(null);

            if (token == null || token.isBlank()) {
                log.error("Fishial.AI 토큰 응답에 token/access_token 필드 없음. 원문: {}", tokenResponse);
                throw new AiServiceException(
                        "Fishial.AI 토큰 응답 형식이 예상과 다릅니다. 원문: " + tokenResponse);
            }

            long expiresIn = json.path("expires_in").asLong(3600);
            cachedToken    = token;
            tokenExpiresAt = Instant.now().plusSeconds(expiresIn - 60); // 60초 여유
            log.info("Fishial.AI Access Token 갱신 완료. 유효시간: {}초", expiresIn);
            return cachedToken;

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Fishial.AI 토큰 응답 파싱 실패: {}, 원문: {}", e.getMessage(), tokenResponse);
            throw new AiServiceException("Fishial.AI 토큰 응답 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * Fishial.AI 인식 API 응답을 {@link AiClassificationResult}로 파싱한다.
     *
     * <p>경로: {@code results[0].species[0]} → common_name, confidence
     * results 또는 species 배열이 비어있으면 "알 수 없음", 0.0 반환.
     */
    private AiClassificationResult parseRecognitionResponse(String responseJson) {
        try {
            JsonNode root    = objectMapper.readTree(responseJson);
            JsonNode results = root.path("results");

            if (results.isMissingNode() || results.isEmpty()) {
                log.info("Fishial.AI 인식 결과 없음 (results 배열 비어있음)");
                return new AiClassificationResult("알 수 없음", 0.0);
            }

            // results[0].species[0] 에서 어종명과 신뢰도를 가져온다
            JsonNode species = results.get(0).path("species");
            if (species.isMissingNode() || species.isEmpty()) {
                log.info("Fishial.AI 인식 결과 없음 (species 배열 비어있음)");
                return new AiClassificationResult("알 수 없음", 0.0);
            }

            JsonNode top        = species.get(0);
            String   fishName   = extractFishName(top);
            double   confidence = top.path("confidence").asDouble(0.0);

            log.info("Fishial.AI 분류 완료: fishName={}, confidence={}", fishName, confidence);
            return new AiClassificationResult(fishName, confidence);

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Fishial.AI 응답 파싱 실패: {}, 원문: {}", e.getMessage(), responseJson);
            throw new AiServiceException("Fishial.AI 응답 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * species[0] 노드에서 어종 일반명(common_name)을 추출한다.
     *
     * <ul>
     *   <li>1순위: {@code common_name} 필드 (예: "Largemouth Bass")</li>
     *   <li>2순위: {@code name} 필드 (학명 폴백, 예: "Micropterus salmoides")</li>
     * </ul>
     */
    private String extractFishName(JsonNode speciesNode) {
        String commonName = speciesNode.path("common_name").asText(null);
        if (commonName != null && !commonName.isBlank()) return commonName;

        String scientificName = speciesNode.path("name").asText(null);
        if (scientificName != null && !scientificName.isBlank()) return scientificName;

        return "알 수 없음";
    }

    /** 토큰 만료 또는 인증 실패 시 캐시를 무효화한다. */
    private synchronized void invalidateToken() {
        cachedToken    = null;
        tokenExpiresAt = Instant.EPOCH;
    }
}
