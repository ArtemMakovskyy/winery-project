package com.winestoreapp.review.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ReviewDto {
    private Long id;
    private Long wineId;
    private Long userId;
    private String message;
    private Integer rating;
    private LocalDate reviewDate;
}
