package com.winestoreapp.wineryadminui.features.order.dto;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Getter;

@Getter
public class ShoppingCardDto {
    private Long id;
    private Set<PurchaseObjectDto> purchaseObjects;
    private BigDecimal totalCost;
}
