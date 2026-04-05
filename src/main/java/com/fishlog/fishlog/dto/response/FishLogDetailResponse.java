package com.fishlog.fishlog.dto.response;

import com.fishlog.fishlog.domain.FishLog;
import com.fishlog.fishlog.domain.enums.WaterType;
import com.fishlog.fishlog.domain.enums.WeatherCondition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FishLogDetailResponse(
        Long id,
        Long userId,
        String title,
        String fishName,
        String fishNameAi,
        LocalDate catchDate,
        String location,
        WaterType waterType,
        Double lengthCm,
        Double weightG,
        String bait,
        WeatherCondition weather,
        Double temperature,
        String memo,
        List<FishPhotoResponse> photos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FishLogDetailResponse from(FishLog fishLog) {
        List<FishPhotoResponse> photos = fishLog.getPhotos().stream()
                .map(FishPhotoResponse::from)
                .toList();

        return new FishLogDetailResponse(
                fishLog.getId(),
                fishLog.getUser().getId(),
                fishLog.getTitle(),
                fishLog.getFishName(),
                fishLog.getFishNameAi(),
                fishLog.getCatchDate(),
                fishLog.getLocation(),
                fishLog.getWaterType(),
                fishLog.getLengthCm(),
                fishLog.getWeightG(),
                fishLog.getBait(),
                fishLog.getWeather(),
                fishLog.getTemperature(),
                fishLog.getMemo(),
                photos,
                fishLog.getCreatedAt(),
                fishLog.getUpdatedAt()
        );
    }
}
