package com.spring_cloud.eureka.client.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    private List<OrderProductListDto> orderList = new ArrayList<>();
    private String status;
}