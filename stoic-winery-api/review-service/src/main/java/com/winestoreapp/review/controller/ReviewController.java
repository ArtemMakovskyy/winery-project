package com.winestoreapp.review.controller;

import com.winestoreapp.common.dto.ResponseErrorDto;
import com.winestoreapp.common.observability.ObservationNames;
import com.winestoreapp.common.observability.ObservationTags;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.review.api.ReviewService;
import com.winestoreapp.review.api.dto.CreateReviewDto;
import com.winestoreapp.review.api.dto.ReviewWithUserDescriptionDto;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Review management", description = "Endpoints to managing reviews")
@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PATCH, RequestMethod.DELETE})
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;
    private final SpanTagger spanTagger;

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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Data for creating a review",
            content = @Content(
                    schema = @Schema(implementation = CreateReviewDto.class),
                    examples = @ExampleObject(
                            name = "Review example",
                            value = """
                            {
                              "wineId": 1,
                              "userFirstAndLastName": "Ivan Petrov",
                              "message": "This is a great wine!",
                              "rating": 5
                            }
                            """
                    )
            )
    )
    @PostMapping
    @Observed(
            name = ObservationNames.REVIEW_CREATE,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public ResponseEntity<ReviewWithUserDescriptionDto> addReview(
            @RequestBody @Valid CreateReviewDto createDto
    ) {
        log.info("REST request to add review for wine ID: {}", createDto.getWineId());

        spanTagger.tag(ObservationTags.WINE_ID, createDto.getWineId());

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
    @Observed(
            name = ObservationNames.REVIEW_FIND_BY_WINE,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.READ}
    )
    public ResponseEntity<List<ReviewWithUserDescriptionDto>> findAllReviewsByWineId(
            @PathVariable("wineId") Long wineId,
            @PageableDefault(size = 4, page = 0, sort = {"reviewDate"},
                    direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("REST request to find reviews for wine ID: {}, pageable: {}", wineId, pageable);

        spanTagger.tag(ObservationTags.WINE_ID, wineId);

        return ResponseEntity.ok(reviewService.findAllByWineId(wineId, pageable));
    }

}