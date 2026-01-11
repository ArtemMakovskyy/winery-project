package com.winestoreapp.order.repository;

import com.winestoreapp.order.model.PurchaseObject;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import io.micrometer.observation.annotation.Observed;

@Observed
public interface PurchaseObjectRepository extends JpaRepository<PurchaseObject, Long> {
    @EntityGraph(attributePaths = {"wine"})
    Optional<PurchaseObject> findById(Long id);
}
