package com.fishlog.fishlog.dto.response.stats;

import com.fishlog.fishlog.domain.enums.WaterType;

public record WaterTypeStatDto(WaterType waterType, long count) {}
