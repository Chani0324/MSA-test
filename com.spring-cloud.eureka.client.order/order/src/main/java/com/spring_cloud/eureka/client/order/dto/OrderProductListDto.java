package com.spring_cloud.eureka.client.order.dto;

import com.spring_cloud.eureka.client.order.entity.OrderProductList;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderProductListDto implements Serializable {
    private UUID productId;
    private int quantity;

    public static OrderProductListDto orderProductFrom(OrderProductList orderProductList) {
        return OrderProductListDto.builder()
                .productId(orderProductList.getProductId())
                .quantity(orderProductList.getQuantity())
                .build();
    }
}
