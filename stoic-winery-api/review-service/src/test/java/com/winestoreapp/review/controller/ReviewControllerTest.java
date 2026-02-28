package com.winestoreapp.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.common.exception.CustomGlobalExceptionHandler;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.review.api.ReviewService;
import com.winestoreapp.review.api.dto.CreateReviewDto;
import com.winestoreapp.review.api.dto.ReviewWithUserDescriptionDto;
import com.winestoreapp.review.config.ReviewControllerTestConfig;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@ContextConfiguration(classes = {
        ReviewControllerTestConfig.class,
        CustomGlobalExceptionHandler.class
})
class ReviewControllerTest {
    @MockBean
    private SpanTagger spanTagger;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @Test
    @WithMockUser
    @DisplayName("POST /reviews - Success")
    void addReview_ValidDto_ShouldReturnCreated() throws Exception {
        CreateReviewDto createDto = new CreateReviewDto();
        createDto.setWineId(1L);
        createDto.setUserFirstAndLastName("Ivan Petrov");
        createDto.setMessage("This is a great wine!");
        createDto.setRating(5);

        ReviewWithUserDescriptionDto responseDto = new ReviewWithUserDescriptionDto();
        responseDto.setId(1L);
        responseDto.setUserFirstName("Ivan");
        responseDto.setUserLastName("Petrov");
        responseDto.setMessage("This is a great wine!");
        responseDto.setRating(5);
        responseDto.setReviewDate(LocalDate.now());

        Mockito.when(reviewService.addReview(any(CreateReviewDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.message").value("This is a great wine!"))
                .andExpect(jsonPath("$.userFirstName").value("Ivan"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /reviews - Invalid Validation (Short Message)")
    void addReview_InvalidDto_ShouldReturnBadRequest() throws Exception {
        CreateReviewDto invalidDto = new CreateReviewDto();
        invalidDto.setWineId(1L);
        invalidDto.setUserFirstAndLastName("Ivan Petrov");
        invalidDto.setMessage("Bad"); // Less than 5 characters
        invalidDto.setRating(5);

        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /reviews/wines/{wineId} - Success")
    void findAllByWineId_ShouldReturnList() throws Exception {
        ReviewWithUserDescriptionDto dto = new ReviewWithUserDescriptionDto();
        dto.setMessage("Excellent choice");
        dto.setRating(4);

        Mockito.when(reviewService.findAllByWineId(anyLong(), any()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/reviews/wines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].message").value("Excellent choice"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /reviews/wines/{wineId} - Empty results")
    void findAllByWineId_Empty_ShouldReturnEmptyList() throws Exception {
        Mockito.when(reviewService.findAllByWineId(anyLong(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/reviews/wines/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /reviews - Forbidden (No User)")
    void addReview_NoAuth_ShouldReturnForbidden() throws Exception {
        CreateReviewDto createDto = new CreateReviewDto();

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }
}
