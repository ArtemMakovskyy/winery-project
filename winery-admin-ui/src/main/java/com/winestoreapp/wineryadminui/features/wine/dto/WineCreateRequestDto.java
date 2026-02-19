package com.winestoreapp.wineryadminui.features.wine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WineCreateRequestDto {
    private String vendorCode;
    private String qualityLevel;
    private String reserveType;
    private String name;
    private String shortName;
    private Integer year;
    private String tasteWine;
    private BigDecimal price;
    private String grape;
    private Boolean isDecantation;
    private WineType wineType;
    private BigDecimal strengthFrom;
    private BigDecimal strengthTo;
    private WineColor wineColor;
    private String colorDescribing;
    private String taste;
    private String aroma;
    private String gastronomy;
    private String description;
    private String pictureLink;
    private String pictureLink2;
}
