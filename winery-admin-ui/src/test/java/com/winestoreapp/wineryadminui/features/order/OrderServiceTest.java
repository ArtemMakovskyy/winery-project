package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderFeignClient orderClient;

    @Mock
    private SpanTagger spanTagger;

    private OrderService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderService(orderClient, spanTagger);
    }

    @Test
    void getAll_success() {
        List<OrderDto> orders = List.of(new OrderDto(), new OrderDto());
        when(orderClient.getAll()).thenReturn(orders);

        List<OrderDto> result = service.getAll();

        assertThat(result).hasSize(2);
        verify(orderClient).getAll();
        verify(spanTagger).tag("orders.count", 2);
    }

    @Test
    void setPaidStatus_success() {
        when(orderClient.setPaidStatus(5L)).thenReturn(true);

        Boolean result = service.setPaidStatus(5L);

        assertThat(result).isTrue();
        verify(spanTagger).tag("order.id", 5L);
        verify(orderClient).setPaidStatus(5L);
    }

    @Test
    void deleteOrder_success() {
        doNothing().when(orderClient).deleteOrder(3L);

        service.deleteOrder(3L);

        verify(spanTagger).tag("order.id", 3L);
        verify(orderClient).deleteOrder(3L);
    }
}
