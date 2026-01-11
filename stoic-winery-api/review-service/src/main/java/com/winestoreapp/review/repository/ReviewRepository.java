package com.winestoreapp.review.repository;

import com.winestoreapp.review.model.Review;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByWineId(Long wineId, Pageable pageable);

    List<Review> findAllByWineId(Long wineId);

    List<Review> findAllByWineIdAndUserId(Long wineId, Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.wineId = :wineId")
    Double findAverageRatingByWineId(@Param("wineId") Long wineId);

    @Query("SELECT MIN(r.id) FROM Review r WHERE r.wineId = :wineId AND r.isDeleted = FALSE")
    Long findMinIdByWineId(@Param("wineId") Long wineId);
}