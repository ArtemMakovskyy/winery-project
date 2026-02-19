package com.winestoreapp.wineryadminui.features.order.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
public class ShoppingCardDto {
    private Long id;
    private Set<PurchaseObjectDto> purchaseObjects;
    private BigDecimal totalCost;
}
