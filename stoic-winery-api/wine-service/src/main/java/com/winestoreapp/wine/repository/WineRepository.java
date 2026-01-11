package com.winestoreapp.wine.repository;

import com.winestoreapp.wine.model.Wine;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import io.micrometer.observation.annotation.Observed;

@Observed
public interface WineRepository extends JpaRepository<Wine, Long> {
    @Modifying
    @Query("UPDATE Wine w SET w.averageRatingScore = :averageRatingScore WHERE w.id = :wineId")
    void updateAverageRatingScore(Long wineId, BigDecimal averageRatingScore);
}
