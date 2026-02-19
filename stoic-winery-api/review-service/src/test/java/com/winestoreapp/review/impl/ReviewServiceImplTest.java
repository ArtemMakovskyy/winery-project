package com.winestoreapp.review.impl;

import com.winestoreapp.common.config.CustomMySqlContainer;
import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.exception.RegistrationException;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.review.api.dto.CreateReviewDto;
import com.winestoreapp.review.api.dto.ReviewWithUserDescriptionDto;
import com.winestoreapp.review.config.TestReviewConfig;
import com.winestoreapp.review.mapper.ReviewMapper;
import com.winestoreapp.review.model.Review;
import com.winestoreapp.review.repository.ReviewRepository;
import com.winestoreapp.user.api.UserService;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.wine.api.WineService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(classes = TestReviewConfig.class)
@ContextConfiguration(initializers = ReviewServiceImplTest.Initializer.class)
@Transactional
class ReviewServiceImplTest {

    @Autowired
    private ReviewServiceImpl reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private WineService wineService;

    @MockBean
    private ReviewMapper reviewMapper;

    @MockBean
    private SpanTagger spanTagger;

    @Test
    @DisplayName("Add review - should return DTO when data is valid")
    void addReview_ValidData_ShouldReturnDto() {
        CreateReviewDto dto = createReviewDto();
        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(1L);
        userDto.setFirstName("Ivan");
        userDto.setLastName("Petrov");

        Mockito.when(wineService.existsById(anyLong())).thenReturn(true);
        Mockito.when(userService.getOrCreateByFirstAndLastName(anyString(), anyString())).thenReturn(userDto);
        Mockito.when(reviewMapper.toUserDescriptionDto(any(Review.class))).thenReturn(new ReviewWithUserDescriptionDto());

        ReviewWithUserDescriptionDto result = reviewService.addReview(dto);

        assertNotNull(result);
        List<Review> savedReviews = reviewRepository.findAllByWineId(1L);
        assertEquals(1, savedReviews.size());
        Mockito.verify(wineService, Mockito.atLeastOnce()).updateAverageRatingScore(anyLong(), anyDouble());
    }

    @Test
    @DisplayName("Add review - should throw RegistrationException when name is invalid")
    void addReview_InvalidName_ShouldThrowRegistrationException() {
        CreateReviewDto dto = createReviewDto();
        dto.setUserFirstAndLastName("Ivan");

        RegistrationException exception = assertThrows(
                RegistrationException.class,
                () -> reviewService.addReview(dto)
        );

        assertEquals("Enter first and last name with a space.", exception.getMessage());
    }

    @Test
    @DisplayName("Add review - should throw EntityNotFoundException when wine does not exist")
    void addReview_NonExistentWine_ShouldThrowEntityNotFoundException() {
        CreateReviewDto dto = createReviewDto();
        Mockito.when(wineService.existsById(anyLong())).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.addReview(dto)
        );

        assertTrue(exception.getMessage().contains("Wine not found"));
    }

    @Test
    @DisplayName("Add review - should enforce limit and delete oldest when exceeded")
    void addReview_ExceedLimit_ShouldDeleteOldest() {
        ReflectionTestUtils.setField(reviewService, "limiter", 1);

        // Save first review directly to DB
        Review first = new Review(1L, 1L, "First", 5);
        reviewRepository.save(first);

        // Prepare to add second review through service
        CreateReviewDto dto = createReviewDto();
        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(2L); // Different user

        Mockito.when(wineService.existsById(anyLong())).thenReturn(true);
        Mockito.when(userService.getOrCreateByFirstAndLastName(anyString(), anyString())).thenReturn(userDto);
        Mockito.when(reviewMapper.toUserDescriptionDto(any())).thenReturn(new ReviewWithUserDescriptionDto());

        reviewService.addReview(dto);

        List<Review> reviews = reviewRepository.findAllByWineId(1L);
        assertEquals(1, reviews.size());
        // Check that the one remaining is NOT the first one (since it should be deleted as MIN id)
        assertTrue(reviews.get(0).getId() > first.getId());
    }

    private CreateReviewDto createReviewDto() {
        CreateReviewDto dto = new CreateReviewDto();
        dto.setWineId(1L);
        dto.setUserFirstAndLastName("Ivan Petrov");
        dto.setMessage("This is a great wine!");
        dto.setRating(5);
        return dto;
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            CustomMySqlContainer container = CustomMySqlContainer.getInstance();
            container.start();

            TestPropertyValues.of(
                    "spring.datasource.url=" + container.getJdbcUrl(),
                    "spring.datasource.username=" + container.getUsername(),
                    "spring.datasource.password=" + container.getPassword(),
                    "spring.jpa.hibernate.ddl-auto=update",
                    "limiter.number.of.recorded.ratings=5"
            ).applyTo(context.getEnvironment());
        }
    }
}
