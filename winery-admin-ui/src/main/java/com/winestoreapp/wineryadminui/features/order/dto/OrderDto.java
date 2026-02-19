package com.winestoreapp.wineryadminui.features.order.dto;

import lombok.Getter;

import java.time.LocalDateTime;

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
