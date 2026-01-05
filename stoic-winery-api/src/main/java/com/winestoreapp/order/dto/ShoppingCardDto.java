package com.winestoreapp.order.dto;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Data;

@Data
public class ShoppingCardDto {
    private Long id;
    private Set<PurchaseObjectDto> purchaseObjects;
    private BigDecimal totalCost;
}
