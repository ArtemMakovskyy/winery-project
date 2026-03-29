package com.winestoreapp.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderEvent extends ApplicationEvent {
    private final Long orderId;
    private final String orderNumber;
    private final Long userId;

    public OrderEvent(Object source, Long orderId, String orderNumber, Long userId) {
        super(source);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
    }
}
