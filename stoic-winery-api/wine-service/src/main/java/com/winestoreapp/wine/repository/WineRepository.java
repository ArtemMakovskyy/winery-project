package com.winestoreapp.wine.repository;

import com.winestoreapp.wine.model.Wine;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WineRepository extends JpaRepository<Wine, Long> {

    @Modifying
    @Query("UPDATE Wine w SET w.averageRatingScore = :rating WHERE w.id = :wineId")
    void updateAverageRating(@Param("wineId") Long wineId, @Param("rating") BigDecimal rating);
}
