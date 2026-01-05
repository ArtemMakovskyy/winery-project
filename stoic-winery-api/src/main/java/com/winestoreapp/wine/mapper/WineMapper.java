package com.winestoreapp.wine.mapper;

import com.winestoreapp.config.MapperConfig;
import com.winestoreapp.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wine.dto.WineDto;
import com.winestoreapp.wine.model.Wine;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WineMapper {

    WineDto toDto(Wine wine);

    Wine toEntity(WineCreateRequestDto dto);
}
