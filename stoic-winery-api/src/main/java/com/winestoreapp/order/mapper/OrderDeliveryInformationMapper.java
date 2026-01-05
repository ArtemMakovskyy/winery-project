package com.winestoreapp.order.mapper;

import com.winestoreapp.config.MapperConfig;
import com.winestoreapp.order.dto.CreateOrderDeliveryInformationDto;
import com.winestoreapp.order.dto.OrderDeliveryInformationDto;
import com.winestoreapp.order.model.OrderDeliveryInformation;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderDeliveryInformationMapper {
    OrderDeliveryInformation toEntity(CreateOrderDeliveryInformationDto dto);

    OrderDeliveryInformationDto toDto(OrderDeliveryInformation odi);
}
