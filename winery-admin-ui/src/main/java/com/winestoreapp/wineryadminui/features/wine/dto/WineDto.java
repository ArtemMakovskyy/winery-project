package com.winestoreapp.wineryadminui.features.wine.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WineDto {
    private Long id;
    private String vendorCode;
    private String qualityLevel;
    private String reserveType;
    private String name;
    private String shortName;
    private Integer year;
    private String grape;
    private String tasteWine;
    private BigDecimal averageRatingScore;
    private BigDecimal price;
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
