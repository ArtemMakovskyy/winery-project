package com.winestoreapp.order.dto;

import java.util.Set;
import lombok.Data;

@Data
public class CreateShoppingCardDto {
    private Set<CreatePurchaseObjectDto> purchaseObjects;
}
