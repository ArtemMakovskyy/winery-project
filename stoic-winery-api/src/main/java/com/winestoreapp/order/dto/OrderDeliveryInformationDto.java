package com.winestoreapp.order.dto;

import lombok.Data;

@Data
public class OrderDeliveryInformationDto {
    private Long id;
    private String zipCode;
    private String region;
    private String city;
    private String street;
    private String comment;
}
