package com.fishlog.fishlog.exception;

/**
 * Fishial.AI 등 외부 AI API 호출 실패 시 발생하는 예외.
 *
 * <p>주요 시나리오:
 * <ul>
 *   <li>401 — client-id / client-secret 인증 실패</li>
 *   <li>402 — 크레딧 부족 또는 구독 만료</li>
 *   <li>429 — 요청 한도(Rate Limit) 초과</li>
 *   <li>5xx — Fishial.AI 서버 오류</li>
 * </ul>
 *
 * <p>업로드 흐름(자동 분류)에서는 {@code ERROR} 레벨 로그로 기록되고 업로드는 성공 처리된다.
 * 명시적 재분류 요청({@code POST /classify})에서는 {@link GlobalExceptionHandler}가 잡아 502로 응답한다.
 */
public class AiServiceException extends RuntimeException {

    private final int statusCode;

    public AiServiceException(String message) {
        super(message);
        this.statusCode = 0;
    }

    public AiServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /** Fishial.AI API가 반환한 HTTP 상태 코드. 직접 호출이 아닌 경우 0. */
    public int getStatusCode() {
        return statusCode;
    }
}
