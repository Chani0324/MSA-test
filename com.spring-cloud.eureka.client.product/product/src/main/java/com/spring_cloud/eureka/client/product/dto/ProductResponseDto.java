package com.spring_cloud.eureka.client.product.dto;

import com.spring_cloud.eureka.client.product.entity.Product;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private UUID id;
    private String name;
    private String description;
    private Integer price;
    private Integer quantity;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public static ProductResponseDto toProductResponseDtoFrom(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCreatedAt(),
                product.getCreatedBy(),
                product.getUpdatedAt(),
                product.getUpdatedBy()
        );
    }
}
