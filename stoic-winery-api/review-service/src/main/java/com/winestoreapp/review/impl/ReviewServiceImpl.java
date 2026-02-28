package com.winestoreapp.review.impl;

import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.exception.RegistrationException;
import com.winestoreapp.common.observability.ObservationNames;
import com.winestoreapp.common.observability.ObservationTags;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.review.api.ReviewService;
import com.winestoreapp.review.api.dto.CreateReviewDto;
import com.winestoreapp.review.api.dto.ReviewWithUserDescriptionDto;
import com.winestoreapp.review.mapper.ReviewMapper;
import com.winestoreapp.review.model.Review;
import com.winestoreapp.review.repository.ReviewRepository;
import com.winestoreapp.user.api.UserService;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.wine.api.WineService;
import io.micrometer.observation.annotation.Observed;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;
    private final WineService wineService;
    private final SpanTagger spanTagger;

    @Value("${limiter.number.of.recorded.ratings}")
    private int limiter;

    @Override
    @Transactional(readOnly = true)
    @Observed(name = ObservationNames.REVIEW_FIND_BY_WINE)
    public List<ReviewWithUserDescriptionDto> findAllByWineId(Long wineId, Pageable pageable) {
        spanTagger.tag(ObservationTags.WINE_ID, wineId);

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
    @Observed(name = ObservationNames.REVIEW_CREATE)
    public ReviewWithUserDescriptionDto addReview(CreateReviewDto dto) {
        log.info("Adding new review for wineId: {}", dto.getWineId());
        spanTagger.tag(ObservationTags.WINE_ID, dto.getWineId());

        String[] nameParts = dto.getUserFirstAndLastName().strip().split("\\s+");
        if (nameParts.length != 2) {
            log.warn("Invalid name format for review: {}", dto.getUserFirstAndLastName());
            throw new RegistrationException("Enter first and last name with a space.");
        }

        if (!wineService.existsById(dto.getWineId())) {
            log.error("Failed to add review. Wine with id {} not found", dto.getWineId());
            throw new EntityNotFoundException("Wine not found");
        }

        UserResponseDto userDto = userService.getOrCreateByFirstAndLastName(nameParts[0], nameParts[1]);
        spanTagger.tag(ObservationTags.USER_ID, userDto.getId());

        removeOutdatedReviews(dto.getWineId(), userDto.getId());

        Review review = new Review(userDto.getId(), dto.getWineId(), dto.getMessage(), dto.getRating());
        Review saved = reviewRepository.save(review);

        calculateWineAverageRatingScoreThenSave(dto.getWineId());

        log.info("Review added successfully for wineId: {} by userId: {}", dto.getWineId(), userDto.getId());

        ReviewWithUserDescriptionDto result = reviewMapper.toUserDescriptionDto(saved);
        result.setUserFirstName(userDto.getFirstName());
        result.setUserLastName(userDto.getLastName());
        return result;
    }

    private void removeOutdatedReviews(Long wineId, Long userId) {
        reviewRepository.findAllByWineIdAndUserId(wineId, userId)
                .forEach(r -> {
                    log.debug("Removing outdated review id: {}", r.getId());
                    reviewRepository.deleteById(r.getId());
                });
    }

    private void calculateWineAverageRatingScoreThenSave(Long wineId) {
        List<Review> reviews = reviewRepository.findAllByWineId(wineId);

        if (reviews.size() > limiter) {
            Long minId = reviewRepository.findMinIdByWineId(wineId);
            log.info("Review limit exceeded for wine {}. Deleting oldest review id: {}", wineId, minId);
            reviewRepository.deleteById(minId);
        }

        Double dbAvg = reviewRepository.findAverageRatingByWineId(wineId);
        double avg = (dbAvg != null ? dbAvg : 0.0);

        wineService.updateAverageRatingScore(wineId, avg);
        log.debug("Updated average rating for wine {}: {}", wineId, avg);
    }

}
