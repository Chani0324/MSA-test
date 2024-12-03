package com.spring_cloud.eureka.client.product.dto;

import com.spring_cloud.eureka.client.product.entity.Product;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ProductRankDto implements Serializable {
    private UUID productId;
    private String name;
    private Integer price;

    public static ProductRankDto fromEntity(Product product) {
        return ProductRankDto.builder()
                .productId(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .build();
    }
}
