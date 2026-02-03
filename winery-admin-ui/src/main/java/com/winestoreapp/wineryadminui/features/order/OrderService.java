package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import feign.FeignException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderFeignClient orderClient;
    private final FeignErrorParser errorParser;

    public List<OrderDto> getAll() {
        log.debug("Fetching all orders from backend");
        return orderClient.getAll();
    }

    public Boolean setPaidStatus(Long id) {
        log.info("Requesting 'PAID' status update for order ID: {}", id);
        try {
            Boolean result = orderClient.setPaidStatus(id);
            log.info("Order ID: {} status update result: {}", id, result);
            return result;
        } catch (FeignException e) {
            String errorMsg = errorParser.extractMessage(e);
            log.error("Failed to set PAID status for order {}: {}", id, errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    public void deleteOrder(Long id) {
        log.info("Requesting deletion of order ID: {}", id);
        try {
            orderClient.deleteOrder(id);
            log.info("Order ID: {} successfully deleted", id);
        } catch (FeignException e) {
            log.error("Failed to delete order ID {}: {}", id, e.getMessage());
            throw new RuntimeException(errorParser.extractMessage(e));
        }
    }
}