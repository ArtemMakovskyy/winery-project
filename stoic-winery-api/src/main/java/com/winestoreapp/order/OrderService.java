package com.winestoreapp.order;

import com.winestoreapp.order.dto.CreateOrderDto;
import com.winestoreapp.order.dto.OrderDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto createOrder(CreateOrderDto dto);

    OrderDto getById(Long id);

    boolean deleteById(Long id);

    boolean updateOrderPaymentStatusAsPaidAndAddCurrentData(Long orderId);

    List<OrderDto> findAll(Pageable pageable);

    List<OrderDto> findAllByUserId(Long userId, Pageable pageable);
}
