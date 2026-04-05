package com.fishlog.fishlog.domain;

import com.fishlog.fishlog.domain.enums.WaterType;
import com.fishlog.fishlog.domain.enums.WeatherCondition;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fish_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FishLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    /** 사용자가 직접 입력한 어종명 */
    @Column(length = 100)
    private String fishName;

    /** AI가 분류한 어종명 */
    @Column(length = 100)
    private String fishNameAi;

    @Column(nullable = false)
    private LocalDate catchDate;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WaterType waterType;

    /** 어체장 (cm) */
    private Double lengthCm;

    /** 무게 (g) */
    private Double weightG;

    @Column(length = 100)
    private String bait;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WeatherCondition weather;

    /** 기온 (℃) */
    private Double temperature;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "fishLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FishPhoto> photos = new ArrayList<>();

    @Builder
    public FishLog(User user, String title, String fishName, LocalDate catchDate,
                   String location, WaterType waterType, Double lengthCm, Double weightG,
                   String bait, WeatherCondition weather, Double temperature, String memo) {
        this.user = user;
        this.title = title;
        this.fishName = fishName;
        this.catchDate = catchDate;
        this.location = location;
        this.waterType = waterType;
        this.lengthCm = lengthCm;
        this.weightG = weightG;
        this.bait = bait;
        this.weather = weather;
        this.temperature = temperature;
        this.memo = memo;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String fishName, LocalDate catchDate, String location,
                       WaterType waterType, Double lengthCm, Double weightG,
                       String bait, WeatherCondition weather, Double temperature, String memo) {
        this.title = title;
        this.fishName = fishName;
        this.catchDate = catchDate;
        this.location = location;
        this.waterType = waterType;
        this.lengthCm = lengthCm;
        this.weightG = weightG;
        this.bait = bait;
        this.weather = weather;
        this.temperature = temperature;
        this.memo = memo;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAiResult(String fishNameAi) {
        this.fishNameAi = fishNameAi;
        this.updatedAt = LocalDateTime.now();
    }
}
