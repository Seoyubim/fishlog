package com.fishlog.fishlog.controller;

import com.fishlog.fishlog.dto.request.FishLogCreateRequest;
import com.fishlog.fishlog.dto.request.FishLogUpdateRequest;
import com.fishlog.fishlog.dto.response.FishLogDetailResponse;
import com.fishlog.fishlog.dto.response.FishLogResponse;
import com.fishlog.fishlog.service.FishLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fish-logs")
@RequiredArgsConstructor
public class FishLogController {

    private final FishLogService fishLogService;

    @PostMapping
    public ResponseEntity<FishLogDetailResponse> create(
            @Valid @RequestBody FishLogCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fishLogService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<FishLogResponse>> getList(
            @RequestParam Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(fishLogService.getList(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FishLogDetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(fishLogService.getDetail(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FishLogDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FishLogUpdateRequest request) {
        return ResponseEntity.ok(fishLogService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fishLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
