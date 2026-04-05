package com.fishlog.fishlog.repository;

import com.fishlog.fishlog.domain.FishPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FishPhotoRepository extends JpaRepository<FishPhoto, Long> {

    List<FishPhoto> findAllByFishLogId(Long fishLogId);
}
