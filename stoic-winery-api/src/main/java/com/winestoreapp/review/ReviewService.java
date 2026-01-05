package com.winestoreapp.review;

import com.winestoreapp.review.dto.CreateReviewDto;
import com.winestoreapp.review.dto.ReviewWithUserDescriptionDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewWithUserDescriptionDto addReview(CreateReviewDto createDto);

    List<ReviewWithUserDescriptionDto> findAllByWineId(Long wineId, Pageable pageable);
}
