package com.winestoreapp.wineryadminui.features.order.dto;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class PurchaseObjectDto {
    private Long wineId;
    private String winePictureLink;
    private String wineName;
    private Integer quantity;
    private BigDecimal price;
}
