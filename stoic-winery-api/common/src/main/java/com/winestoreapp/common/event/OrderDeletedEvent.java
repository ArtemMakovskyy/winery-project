package com.winestoreapp.common.event;

public class OrderDeletedEvent extends OrderEvent {
    public OrderDeletedEvent(Object source, Long orderId, String orderNumber, Long userId) {
        super(source, orderId, orderNumber, userId);
    }
}
