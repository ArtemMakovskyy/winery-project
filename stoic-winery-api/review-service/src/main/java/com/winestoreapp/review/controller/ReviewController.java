package com.winestoreapp.review.controller;

import com.winestoreapp.common.dto.ResponseErrorDto;
import com.winestoreapp.review.api.ReviewService;
import com.winestoreapp.review.api.dto.CreateReviewDto;
import com.winestoreapp.review.api.dto.ReviewWithUserDescriptionDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Review management", description = "Endpoints to managing reviews")
@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PATCH, RequestMethod.DELETE})
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;
    private final Tracer tracer;

    @Operation(summary = "Add review to wine.",
            description = """
                    Adds a review to wine from a specific User. A specific user can't leave more
                     than one review of one kind of wine. If a review already exists, an earlier
                     review with a rating is deleted, new adds. If there is no user with first
                     and last name, a new one is created. Users are compared by
                     first and last name. Available for all users.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewWithUserDescriptionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid validation or data conflict",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @PostMapping
    @Observed(name = "review.controller", contextualName = "add-review")
    public ResponseEntity<ReviewWithUserDescriptionDto> addReview(
            @RequestBody @Valid CreateReviewDto createDto
    ) {
        log.info("REST request to add review for wine ID: {}", createDto.getWineId());
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("wine.id", String.valueOf(createDto.getWineId()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.addReview(createDto));
    }

    @Operation(summary = "Find all reviews by wine id.",
            description = """
                    Find all reviews by wine id, sort by reviewDate.DESC, size = 4, page = 0.
                    Pagination example: /reviews/wine/{wineId}?size=5&page=0&sort=id
                    Available for all users""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of reviews returned",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReviewWithUserDescriptionDto.class)))),
            @ApiResponse(responseCode = "404", description = "Wine not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @GetMapping("/wines/{wineId}")
    @Observed(name = "review.controller", contextualName = "find-reviews-by-wine")
    public ResponseEntity<List<ReviewWithUserDescriptionDto>> findAllReviewsByWineId(
            @PathVariable("wineId") Long wineId,
            @PageableDefault(size = 4, page = 0, sort = {"reviewDate"},
                    direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("REST request to find reviews for wine ID: {}, pageable: {}", wineId, pageable);
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("wine.id", String.valueOf(wineId));
        }
        return ResponseEntity.ok(reviewService.findAllByWineId(wineId, pageable));
    }
}
