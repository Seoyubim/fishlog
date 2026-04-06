package com.fishlog.fishlog.service;

import com.fishlog.fishlog.domain.FishLog;
import com.fishlog.fishlog.domain.User;
import com.fishlog.fishlog.dto.request.FishLogCreateRequest;
import com.fishlog.fishlog.dto.request.FishLogUpdateRequest;
import com.fishlog.fishlog.dto.response.FishLogDetailResponse;
import com.fishlog.fishlog.dto.response.FishLogResponse;
import com.fishlog.fishlog.exception.FishLogNotFoundException;
import com.fishlog.fishlog.exception.UserNotFoundException;
import com.fishlog.fishlog.repository.FishLogRepository;
import com.fishlog.fishlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FishLogService {

    private final FishLogRepository fishLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public FishLogDetailResponse create(FishLogCreateRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        FishLog fishLog = FishLog.builder()
                .user(user)
                .title(request.title())
                .fishName(request.fishName())
                .catchDate(request.catchDate())
                .location(request.location())
                .waterType(request.waterType())
                .lengthCm(request.lengthCm())
                .weightG(request.weightG())
                .bait(request.bait())
                .weather(request.weather())
                .temperature(request.temperature())
                .memo(request.memo())
                .build();

        return FishLogDetailResponse.from(fishLogRepository.save(fishLog));
    }

    public Page<FishLogResponse> getList(Pageable pageable) {
        Long userId = getCurrentUserId();
        return fishLogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(FishLogResponse::from);
    }

    public FishLogDetailResponse getDetail(Long id) {
        FishLog fishLog = fishLogRepository.findById(id)
                .orElseThrow(() -> new FishLogNotFoundException(id));
        return FishLogDetailResponse.from(fishLog);
    }

    @Transactional
    public FishLogDetailResponse update(Long id, FishLogUpdateRequest request) {
        FishLog fishLog = fishLogRepository.findById(id)
                .orElseThrow(() -> new FishLogNotFoundException(id));

        fishLog.update(
                request.title(),
                request.fishName(),
                request.catchDate(),
                request.location(),
                request.waterType(),
                request.lengthCm(),
                request.weightG(),
                request.bait(),
                request.weather(),
                request.temperature(),
                request.memo()
        );

        return FishLogDetailResponse.from(fishLog);
    }

    @Transactional
    public void delete(Long id) {
        if (!fishLogRepository.existsById(id)) {
            throw new FishLogNotFoundException(id);
        }
        fishLogRepository.deleteById(id);
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
