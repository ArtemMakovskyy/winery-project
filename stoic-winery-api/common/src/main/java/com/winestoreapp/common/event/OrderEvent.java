package com.winestoreapp.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderEvent extends ApplicationEvent {
    private final Long orderId;
    private final String orderNumber;
    private final Long userId;
    private final OrderAccessType accessType;

    public OrderEvent(Object source, Long orderId, String orderNumber, Long userId, OrderAccessType accessType) {
        super(source);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.accessType = accessType;
    }
}
