package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "orderFeignClient",
        url = "${api.backend-url}/orders"
)
public interface OrderFeignClient {

    @PatchMapping("/{id}/paid")
    Boolean setPaidStatus(@PathVariable("id") Long id);

    @DeleteMapping("/{id}")
    void deleteOrder(@PathVariable("id") Long id);

    @GetMapping
    List<OrderDto> getAll();
}
