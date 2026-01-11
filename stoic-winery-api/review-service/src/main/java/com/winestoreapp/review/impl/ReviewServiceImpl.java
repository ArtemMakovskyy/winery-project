package com.winestoreapp.review.impl;

import com.winestoreapp.review.api.dto.CreateReviewDto;
import com.winestoreapp.review.api.dto.ReviewWithUserDescriptionDto;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.common.exception.RegistrationException;
import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.review.api.ReviewService;
import com.winestoreapp.review.mapper.ReviewMapper;
import com.winestoreapp.review.model.Review;
import com.winestoreapp.review.repository.ReviewRepository;
import com.winestoreapp.user.api.UserService;
import com.winestoreapp.wine.api.WineService;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;
    private final WineService wineService;

    @Value("${limiter.number.of.recorded.ratings}")
    private int limiter;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewWithUserDescriptionDto> findAllByWineId(Long wineId, Pageable pageable) {
        List<Review> reviews = reviewRepository.findAllByWineId(wineId, pageable).getContent();

        Map<Long, UserResponseDto> usersCache = new HashMap<>();

        return reviews.stream()
                .map(review -> {
                    ReviewWithUserDescriptionDto dto = reviewMapper.toUserDescriptionDto(review);

                    UserResponseDto userDto = usersCache.computeIfAbsent(review.getUserId(),
                            userService::loadUserById);

                    dto.setUserFirstName(userDto.getFirstName());
                    dto.setUserLastName(userDto.getLastName());
                    return dto;
                }).toList();
    }

    @Override
    @Transactional
    public ReviewWithUserDescriptionDto addReview(CreateReviewDto dto) {
        String[] nameParts = dto.getUserFirstAndLastName().strip().split("\\s+");
        if (nameParts.length != 2) {
            throw new RegistrationException("Enter first and last name with a space.");
        }

        if (!wineService.existsById(dto.getWineId())) {
            throw new EntityNotFoundException("Wine not found");
        }

        UserResponseDto userDto = userService.getOrCreateByFirstAndLastName(nameParts[0], nameParts[1]);

        removeOutdatedReviews(dto.getWineId(), userDto.getId());

        Review review = new Review(userDto.getId(), dto.getWineId(), dto.getMessage(), dto.getRating());
        Review saved = reviewRepository.save(review);

        calculateWineAverageRatingScoreThenSave(dto.getWineId());

        ReviewWithUserDescriptionDto result = reviewMapper.toUserDescriptionDto(saved);
        result.setUserFirstName(userDto.getFirstName());
        result.setUserLastName(userDto.getLastName());
        return result;
    }

    private void removeOutdatedReviews(Long wineId, Long userId) {
        reviewRepository.findAllByWineIdAndUserId(wineId, userId)
                .forEach(r -> reviewRepository.deleteById(r.getId()));
    }

    private void calculateWineAverageRatingScoreThenSave(Long wineId) {
        List<Review> reviews = reviewRepository.findAllByWineId(wineId);

        if (reviews.size() > limiter) {
            reviewRepository.deleteById(reviewRepository.findMinIdByWineId(wineId));
        }

        Double dbAvg = reviewRepository.findAverageRatingByWineId(wineId);
        double avg = (dbAvg != null ? dbAvg : 0.0);

        wineService.updateAverageRatingScore(wineId, avg);
    }
}