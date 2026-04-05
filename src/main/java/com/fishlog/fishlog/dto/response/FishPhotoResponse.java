package com.fishlog.fishlog.dto.response;

import com.fishlog.fishlog.domain.FishPhoto;

import java.time.LocalDateTime;

public record FishPhotoResponse(
        Long id,
        String photoUrl,
        boolean aiClassified,
        Double aiConfidence,
        LocalDateTime createdAt
) {
    public static FishPhotoResponse from(FishPhoto photo) {
        return new FishPhotoResponse(
                photo.getId(),
                photo.getPhotoUrl(),
                photo.isAiClassified(),
                photo.getAiConfidence(),
                photo.getCreatedAt()
        );
    }
}
