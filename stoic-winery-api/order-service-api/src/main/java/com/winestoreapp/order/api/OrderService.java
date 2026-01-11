package com.winestoreapp.order.api;

import com.winestoreapp.order.api.dto.CreateOrderDto;
import com.winestoreapp.order.api.dto.OrderDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto createOrder(CreateOrderDto dto);

    OrderDto getById(Long id);

    boolean deleteById(Long id);

    boolean markAsPaid(Long orderId);

    List<OrderDto> findAll(Pageable pageable);

    List<OrderDto> findAllByUserId(Long userId, Pageable pageable);

    Optional<OrderDto> findByOrderNumber(String orderNumber);
}
