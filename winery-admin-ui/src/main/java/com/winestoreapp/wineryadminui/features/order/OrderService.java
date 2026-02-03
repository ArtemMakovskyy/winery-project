package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderFeignClient orderClient;
    private final FeignErrorParser errorParser;
    private final Tracer tracer;

    @Observed(name = "order.service.get_all")
    public List<OrderDto> getAll() {
        var span = tracer.currentSpan();
        try {
            log.debug("Fetching all orders");
            List<OrderDto> orders = orderClient.getAll();

            if (span != null) {
                span.tag("status", "success");
                span.tag("orders.count", String.valueOf(orders.size()));
            }

            return orders;
        } catch (Exception e) {
            log.error("Failed to fetch orders", e);
            if (span != null) {
                span.tag("status", "error");
                span.error(e);
            }
            throw new RuntimeException("Failed to fetch orders: " + e.getMessage(), e);
        }
    }

    @Observed(name = "order.service.set_paid")
    public Boolean setPaidStatus(Long id) {
        var span = tracer.currentSpan();
        try {
            log.info("Setting PAID status for order {}", id);
            Boolean result = orderClient.setPaidStatus(id);

            if (span != null) {
                span.tag("status", "success");
                span.tag("order.id", String.valueOf(id));
            }

            return result;
        } catch (FeignException e) {
            String msg = errorParser.extractMessage(e);
            log.error("Failed to set PAID status for order {}: {}", id, msg);
            if (span != null) {
                span.tag("status", "error");
                span.error(e);
                span.tag("order.id", String.valueOf(id));
            }
            throw new RuntimeException(msg, e);
        }
    }

    @Observed(name = "order.service.delete")
    public void deleteOrder(Long id) {
        var span = tracer.currentSpan();
        try {
            log.info("Deleting order {}", id);
            orderClient.deleteOrder(id);

            if (span != null) {
                span.tag("status", "success");
                span.tag("order.id", String.valueOf(id));
            }

        } catch (FeignException e) {
            String msg = errorParser.extractMessage(e);
            log.error("Failed to delete order {}: {}", id, msg);
            if (span != null) {
                span.tag("status", "error");
                span.error(e);
                span.tag("order.id", String.valueOf(id));
            }
            throw new RuntimeException(msg, e);
        }
    }
}
