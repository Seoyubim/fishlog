package com.fishlog.fishlog.dto.response;

import com.fishlog.fishlog.domain.FishLog;
import com.fishlog.fishlog.domain.enums.WaterType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FishLogResponse(
        Long id,
        String title,
        String fishName,        // AI 어종명 우선, 없으면 직접 입력값
        LocalDate catchDate,
        String location,
        WaterType waterType,
        Double lengthCm,
        Double weightG,
        int photoCount,
        LocalDateTime createdAt
) {
    public static FishLogResponse from(FishLog fishLog) {
        String displayFishName = fishLog.getFishNameAi() != null
                ? fishLog.getFishNameAi()
                : fishLog.getFishName();

        return new FishLogResponse(
                fishLog.getId(),
                fishLog.getTitle(),
                displayFishName,
                fishLog.getCatchDate(),
                fishLog.getLocation(),
                fishLog.getWaterType(),
                fishLog.getLengthCm(),
                fishLog.getWeightG(),
                fishLog.getPhotos().size(),
                fishLog.getCreatedAt()
        );
    }
}
