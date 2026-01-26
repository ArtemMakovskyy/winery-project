package com.winestoreapp.wineryadminui.features.order.dto;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long userId;
    private ShoppingCardDto shoppingCardDto;
    private OrderDeliveryInformationDto orderDeliveryInformationDto;
    private LocalDateTime registrationTime;
    private LocalDateTime completedTime;
    private OrderPaymentStatus paymentStatus;
}
