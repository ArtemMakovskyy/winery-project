package com.winestoreapp.common.event;

public class OrderCreatedEvent extends OrderEvent {
    public OrderCreatedEvent(Object source, Long orderId, String orderNumber, Long userId) {
        super(source, orderId, orderNumber, userId);
    }
}
