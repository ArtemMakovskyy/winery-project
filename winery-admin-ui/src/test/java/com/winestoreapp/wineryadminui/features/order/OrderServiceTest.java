package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import feign.FeignException;
import feign.Request;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Mock
    private OrderFeignClient orderClient;

    @Mock
    private FeignErrorParser errorParser;

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    private OrderService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(tracer.currentSpan()).thenReturn(span);
        service = new OrderService(orderClient, errorParser, tracer);
    }

    private FeignException createFeignException(int status, String message) {
        Request request = Request.create(Request.HttpMethod.GET, "/api/orders",
                Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        return FeignException.errorStatus("test", feign.Response.builder()
                .status(status)
                .reason(message)
                .request(request)
                .build());
    }

    @Test
    void getAll_success() {
        List<OrderDto> orders = List.of(new OrderDto(), new OrderDto());
        when(orderClient.getAll()).thenReturn(orders);

        List<OrderDto> result = service.getAll();

        assertThat(result).hasSize(2);
        verify(orderClient).getAll();
        verify(span).tag("status", "success");
        verify(span).tag("orders.count", "2");
    }

    @Test
    void getAll_failure_shouldThrowRuntimeException() {
        RuntimeException ex = new RuntimeException("Backend down");
        when(orderClient.getAll()).thenThrow(ex);

        assertThatThrownBy(() -> service.getAll())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch orders");

        verify(span).tag("status", "error");
        verify(span).error(ex);
    }

    @Test
    void setPaidStatus_success() {
        when(orderClient.setPaidStatus(5L)).thenReturn(true);

        Boolean result = service.setPaidStatus(5L);

        assertThat(result).isTrue();
        verify(orderClient).setPaidStatus(5L);
        verify(span).tag("status", "success");
        verify(span).tag("order.id", "5");
    }

    @Test
    void setPaidStatus_feignError_shouldUseErrorParserAndThrow() {
        FeignException ex = createFeignException(404, "Not Found");
        when(orderClient.setPaidStatus(5L)).thenThrow(ex);
        when(errorParser.extractMessage(ex)).thenReturn("Order not found");

        assertThatThrownBy(() -> service.setPaidStatus(5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order not found");

        verify(errorParser).extractMessage(ex);
        verify(span).tag("status", "error");
        verify(span).error(ex);
    }

    @Test
    void deleteOrder_success() {
        doNothing().when(orderClient).deleteOrder(3L);

        service.deleteOrder(3L);

        verify(orderClient).deleteOrder(3L);
        verify(span).tag("status", "success");
        verify(span).tag("order.id", "3");
    }

    @Test
    void deleteOrder_feignError_shouldThrowRuntimeException() {
        FeignException ex = createFeignException(500, "Internal Server Error");
        doThrow(ex).when(orderClient).deleteOrder(3L);
        when(errorParser.extractMessage(ex)).thenReturn("Delete failed");

        assertThatThrownBy(() -> service.deleteOrder(3L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Delete failed");

        verify(errorParser).extractMessage(ex);
        verify(span).tag("status", "error");
        verify(span).error(ex);
    }
}