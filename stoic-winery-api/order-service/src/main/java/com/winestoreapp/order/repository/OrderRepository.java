package com.winestoreapp.order.repository;

import com.winestoreapp.order.model.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"shoppingCard.purchaseObjects"})
    Optional<Order> findById(Long id);

    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"shoppingCard"})
    Optional<Order> findOrderByOrderNumber(String orderNumber);
}
