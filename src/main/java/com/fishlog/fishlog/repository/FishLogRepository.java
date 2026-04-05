package com.fishlog.fishlog.repository;

import com.fishlog.fishlog.domain.FishLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FishLogRepository extends JpaRepository<FishLog, Long> {

    Page<FishLog> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // ── 4단계 통계 쿼리 ──────────────────────────────────────────────────────

    /** 연간 총 마릿수 */
    @Query("SELECT COUNT(f) FROM FishLog f WHERE f.user.id = :userId AND YEAR(f.catchDate) = :year")
    long countByUserIdAndYear(@Param("userId") Long userId, @Param("year") int year);

    /** 연간 최대 어체장 */
    @Query("SELECT MAX(f.lengthCm) FROM FishLog f WHERE f.user.id = :userId AND YEAR(f.catchDate) = :year")
    Double maxLengthByUserIdAndYear(@Param("userId") Long userId, @Param("year") int year);

    /** 연간 평균 어체장 */
    @Query("SELECT AVG(f.lengthCm) FROM FishLog f WHERE f.user.id = :userId AND YEAR(f.catchDate) = :year AND f.lengthCm IS NOT NULL")
    Double avgLengthByUserIdAndYear(@Param("userId") Long userId, @Param("year") int year);

    /** 월별 마릿수 집계 */
    @Query("SELECT MONTH(f.catchDate) AS month, COUNT(f) AS cnt " +
           "FROM FishLog f WHERE f.user.id = :userId AND YEAR(f.catchDate) = :year " +
           "GROUP BY MONTH(f.catchDate) ORDER BY MONTH(f.catchDate)")
    List<Object[]> countByMonth(@Param("userId") Long userId, @Param("year") int year);

    /** 어종별 포획 횟수 순위 */
    @Query("SELECT COALESCE(f.fishNameAi, f.fishName) AS name, COUNT(f) AS cnt " +
           "FROM FishLog f WHERE f.user.id = :userId " +
           "AND (f.fishName IS NOT NULL OR f.fishNameAi IS NOT NULL) " +
           "GROUP BY COALESCE(f.fishNameAi, f.fishName) ORDER BY cnt DESC")
    List<Object[]> fishRankByUserId(@Param("userId") Long userId);

    /** 수계별(민물/바다) 비율 */
    @Query("SELECT f.waterType, COUNT(f) FROM FishLog f WHERE f.user.id = :userId GROUP BY f.waterType")
    List<Object[]> waterTypeStatsByUserId(@Param("userId") Long userId);
}
