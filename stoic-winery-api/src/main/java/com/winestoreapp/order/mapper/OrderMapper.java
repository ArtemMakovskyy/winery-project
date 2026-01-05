package com.winestoreapp.order.mapper;

import com.winestoreapp.config.MapperConfig;
import com.winestoreapp.order.dto.OrderDto;
import com.winestoreapp.order.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = ShoppingCardMapper.class
)
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "shoppingCardDto", source = "shoppingCard")
    @Mapping(target = "orderDeliveryInformationDto", source = "deliveryInformation")
    OrderDto toDto(Order order);
}
