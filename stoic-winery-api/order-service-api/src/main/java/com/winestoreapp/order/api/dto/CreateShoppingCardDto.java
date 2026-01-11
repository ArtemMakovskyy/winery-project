package com.winestoreapp.order.api.dto;

import java.util.Set;
import lombok.Data;

@Data
public class CreateShoppingCardDto {
    private Set<CreatePurchaseObjectDto> purchaseObjects;
}
