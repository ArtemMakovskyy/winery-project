package com.winestoreapp.review.api;

import com.winestoreapp.review.api.dto.CreateReviewDto;
import com.winestoreapp.review.api.dto.ReviewWithUserDescriptionDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewWithUserDescriptionDto addReview(CreateReviewDto createDto);

    List<ReviewWithUserDescriptionDto> findAllByWineId(Long wineId, Pageable pageable);
}
