package com.spring_cloud.eureka.client.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.spring_cloud.eureka.client.product.entity.Product;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
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

    @QueryProjection
    public ProductResponseDto(UUID id, String name, String description, Integer price, Integer quantity, LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt, String updatedBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

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
