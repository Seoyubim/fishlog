package com.fishlog.fishlog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fish_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FishPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fish_log_id", nullable = false)
    private FishLog fishLog;

    @Column(nullable = false, length = 500)
    private String photoUrl;

    /** AI 분류 완료 여부 */
    @Column(nullable = false)
    private boolean aiClassified = false;

    /** AI 분류 신뢰도 (0.0 ~ 1.0) */
    private Double aiConfidence;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public FishPhoto(FishLog fishLog, String photoUrl) {
        this.fishLog = fishLog;
        this.photoUrl = photoUrl;
        this.createdAt = LocalDateTime.now();
    }

    public void applyAiResult(Double aiConfidence) {
        this.aiClassified = true;
        this.aiConfidence = aiConfidence;
    }
}
