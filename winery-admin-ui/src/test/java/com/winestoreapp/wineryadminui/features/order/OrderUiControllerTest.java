package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUiControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private SpanTagger spanTagger;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private OrderUiController orderUiController;

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
        verify(spanTagger).tag("order.id", orderId);
        verify(redirectAttributes).addFlashAttribute("message", "Order marked as paid");
    }

    @Test
    void setPaid_OnException_ShouldPropagateException() {
        Long orderId = 1L;
        RuntimeException exception = new RuntimeException("Payment error");
        when(orderService.setPaidStatus(orderId)).thenThrow(exception);

        assertThatThrownBy(() -> orderUiController.setPaid(orderId, redirectAttributes))
                .isEqualTo(exception);

        verify(spanTagger).tag("order.id", orderId);
    }

    @Test
    void delete_OnSuccess_ShouldTagSpanAndRedirect() {
        Long orderId = 7L;
        doNothing().when(orderService).deleteOrder(orderId);

        String view = orderUiController.delete(orderId, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/ui/orders");
        verify(spanTagger).tag("order.id", orderId);
        verify(redirectAttributes).addFlashAttribute("message", "Order deleted");
    }

    @Test
    void delete_OnException_ShouldPropagateException() {
        Long orderId = 7L;
        RuntimeException exception = new RuntimeException("Delete failed");
        doThrow(exception).when(orderService).deleteOrder(orderId);

        assertThatThrownBy(() -> orderUiController.delete(orderId, redirectAttributes))
                .isEqualTo(exception);

        verify(spanTagger).tag("order.id", orderId);
    }
}