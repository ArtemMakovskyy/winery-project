package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import feign.FeignException;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {

    private final OrderFeignClient orderClient;
    private final FeignErrorParser errorParser;

    public List<OrderDto> getAll() {
        log.info("Fetching all orders");
        return orderClient.getAll();
    }

    public Boolean setPaidStatus(Long id) {
        try {
            return orderClient.setPaidStatus(id);
        } catch (FeignException e) {
            throw new RuntimeException(errorParser.extractMessage(e));
        }
    }

    public void deleteOrder(Long id) {
        orderClient.deleteOrder(id);
    }
}
