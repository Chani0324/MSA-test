package com.spring_cloud.eureka.client.product.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class ProductRequestDto {
    private String name;
    private String description;
    private Integer price;
    private Integer quantity;
}
