package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderUiControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private Tracer tracer;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Span span;

    @InjectMocks
    private OrderUiController orderUiController;

    @BeforeEach
    void setup() {
        lenient().when(tracer.currentSpan()).thenReturn(span);
    }

    @Test
    void list_ShouldReturnOrdersViewAndPopulateModel() {
        List<OrderDto> orders = List.of(new OrderDto(), new OrderDto());
        when(orderService.getAll()).thenReturn(orders);

        String view = orderUiController.list(model);

        assertThat(view).isEqualTo("order/orders");
        verify(model).addAttribute("orders", orders);
    }

    @Test
    void setPaid_OnSuccess_ShouldTagSpanAndRedirect() {
        Long orderId = 1L;
        when(orderService.setPaidStatus(orderId)).thenReturn(true);

        String view = orderUiController.setPaid(orderId, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/ui/orders");
        verify(span).tag("order.id", String.valueOf(orderId));
        verify(redirectAttributes).addFlashAttribute("message", "Order marked as paid");
    }

    @Test
    void setPaid_OnException_ShouldAddErrorToFlashAttributes() {
        Long orderId = 1L;
        when(orderService.setPaidStatus(orderId)).thenThrow(new RuntimeException("Payment error"));

        orderUiController.setPaid(orderId, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "Failed to update status: Payment error");
        verify(redirectAttributes, never()).addFlashAttribute(eq("message"), any());
    }

    @Test
    void delete_OnSuccess_ShouldTagSpanAndRedirect() {
        Long orderId = 7L;
        doNothing().when(orderService).deleteOrder(orderId);

        String view = orderUiController.delete(orderId, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/ui/orders");
        verify(span).tag("order.id", String.valueOf(orderId));
        verify(redirectAttributes).addFlashAttribute("message", "Order deleted");
    }

    @Test
    void delete_OnException_ShouldAddErrorToFlashAttributes() {
        Long orderId = 7L;
        doThrow(new RuntimeException("Delete failed")).when(orderService).deleteOrder(orderId);

        orderUiController.delete(orderId, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "Failed to delete order");
        verify(redirectAttributes, never()).addFlashAttribute(eq("message"), any());
    }
}