package com.winestoreapp.common.event;

public class OrderPaidEvent extends OrderEvent {
    public OrderPaidEvent(Object source, Long orderId, String orderNumber, Long userId) {
        super(source, orderId, orderNumber, userId);
    }
}
