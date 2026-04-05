package com.fishlog.fishlog.dto.request;

import com.fishlog.fishlog.domain.enums.WaterType;
import com.fishlog.fishlog.domain.enums.WeatherCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record FishLogUpdateRequest(

        @NotBlank(message = "제목은 필수입니다")
        String title,

        String fishName,

        @NotNull(message = "낚은 날짜는 필수입니다")
        @PastOrPresent(message = "낚은 날짜는 오늘 이전이어야 합니다")
        LocalDate catchDate,

        String location,
        WaterType waterType,

        @Positive(message = "어체장은 양수여야 합니다")
        Double lengthCm,

        @Positive(message = "무게는 양수여야 합니다")
        Double weightG,

        String bait,
        WeatherCondition weather,
        Double temperature,
        String memo
) {}
