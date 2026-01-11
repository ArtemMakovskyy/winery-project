package com.winestoreapp.order.mapper;

import com.winestoreapp.common.config.MapperConfig;
import com.winestoreapp.order.api.dto.PurchaseObjectDto;
import com.winestoreapp.order.model.PurchaseObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseObjectMapper {

    @Mapping(target = "wineId", source = "purchaseObject.wineId")
    PurchaseObjectDto toDto(PurchaseObject purchaseObject);
}