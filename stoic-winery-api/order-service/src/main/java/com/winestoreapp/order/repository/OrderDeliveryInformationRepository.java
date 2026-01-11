package com.winestoreapp.order.repository;

import com.winestoreapp.order.model.OrderDeliveryInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import io.micrometer.observation.annotation.Observed;

@Observed
public interface OrderDeliveryInformationRepository
        extends JpaRepository<OrderDeliveryInformation, Long> {
}
