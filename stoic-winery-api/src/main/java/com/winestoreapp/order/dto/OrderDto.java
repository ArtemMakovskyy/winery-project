package com.winestoreapp.order.dto;

import com.winestoreapp.order.model.OrderPaymentStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
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
