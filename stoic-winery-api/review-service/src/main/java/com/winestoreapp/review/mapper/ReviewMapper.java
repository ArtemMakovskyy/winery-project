package com.winestoreapp.review.mapper;

import com.winestoreapp.common.config.MapperConfig;
import com.winestoreapp.review.api.dto.ReviewDto;
import com.winestoreapp.review.api.dto.ReviewWithUserDescriptionDto;
import com.winestoreapp.review.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    ReviewDto toDto(Review review);

    ReviewWithUserDescriptionDto toUserDescriptionDto(Review review);
}
