package com.winestoreapp.order.mapper;

import com.winestoreapp.common.config.MapperConfig;
import com.winestoreapp.order.api.dto.ShoppingCardDto;
import com.winestoreapp.order.model.ShoppingCard;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = PurchaseObjectMapper.class)
public interface ShoppingCardMapper {

    ShoppingCardDto toDto(ShoppingCard shoppingCard);
}
