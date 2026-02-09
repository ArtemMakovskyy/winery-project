package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderFeignClient orderClient;
    private final FeignErrorParser errorParser;
    private final Tracer tracer;

    @Observed(name = "order.service.get_all")
    public List<OrderDto> getAll() {
        try {
            log.debug("Fetching all orders");
            List<OrderDto> orders = orderClient.getAll();

            tagSpan("status", "success");
            tagSpan("orders.count", orders.size());

            return orders;
        } catch (Exception e) {
            log.error("Failed to fetch orders", e);
            tagSpan("status", "error");
            recordError(e);
            throw new RuntimeException("Failed to fetch orders: " + e.getMessage(), e);
        }
    }

    @Observed(name = "order.service.set_paid")
    public Boolean setPaidStatus(Long id) {
        tagSpan("order.id", id);
        try {
            log.info("Setting PAID status for order {}", id);
            Boolean result = orderClient.setPaidStatus(id);

            tagSpan("status", "success");
            return result;
        } catch (FeignException e) {
            String msg = errorParser.extractMessage(e);
            log.error("Failed to set PAID status for order {}: {}", id, msg);

            tagSpan("status", "error");
            recordError(e);

            throw new RuntimeException(msg, e);
        }
    }

    @Observed(name = "order.service.delete")
    public void deleteOrder(Long id) {
        tagSpan("order.id", id);
        try {
            log.info("Deleting order {}", id);
            orderClient.deleteOrder(id);

            tagSpan("status", "success");
        } catch (FeignException e) {
            String msg = errorParser.extractMessage(e);
            log.error("Failed to delete order {}: {}", id, msg);

            tagSpan("status", "error");
            recordError(e);

            throw new RuntimeException(msg, e);
        }
    }

    private void tagSpan(String key, Object value) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.tag(key, String.valueOf(value));
        }
    }

    private void recordError(Throwable e) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.error(e);
        }
    }
}