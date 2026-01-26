package com.winestoreapp.wineryadminui.features.order.dto;

import lombok.Getter;

@Getter
public class OrderDeliveryInformationDto {
    private Long id;
    private String zipCode;
    private String region;
    private String city;
    private String street;
    private String comment;
}
