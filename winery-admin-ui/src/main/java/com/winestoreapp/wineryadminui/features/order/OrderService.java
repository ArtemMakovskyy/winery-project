package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {

    private final OrderFeignClient orderClient;

    public List<OrderDto> getAll() {
        log.info("Fetching all orders");
        return orderClient.getAll();
    }

    public Boolean setPaidStatus(Long id) {
        return orderClient.setPaidStatus(id);
    }

    public void deleteOrder(Long id) {
        orderClient.deleteOrder(id);
    }
}
