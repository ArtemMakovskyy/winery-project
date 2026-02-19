package com.winestoreapp.wineryadminui.features.order.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PurchaseObjectDto {
    private Long wineId;
    private String winePictureLink;
    private String wineName;
    private Integer quantity;
    private BigDecimal price;
}
