package com.winestoreapp.review.mapper;

import com.winestoreapp.config.MapperConfig;
import com.winestoreapp.review.dto.ReviewDto;
import com.winestoreapp.review.dto.ReviewWithUserDescriptionDto;
import com.winestoreapp.review.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "wineId", source = "wine.id")
    ReviewDto toDto(Review review);

    @Mapping(target = "userFirstName", source = "user.firstName")
    @Mapping(target = "userLastName", source = "user.lastName")
    ReviewWithUserDescriptionDto toUserDescriptionDto(Review review);

}
