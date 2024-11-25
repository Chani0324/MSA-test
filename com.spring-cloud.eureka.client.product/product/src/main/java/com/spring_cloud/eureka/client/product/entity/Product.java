package com.spring_cloud.eureka.client.product.entity;

import com.spring_cloud.eureka.client.product.dto.ProductRequestDto;
import com.spring_cloud.eureka.client.product.dto.ProductResponseDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    private Integer price;
    private Integer quantity;

    public static Product createProductOf(ProductRequestDto requestDto, String email) {
        return Product.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .quantity(requestDto.getQuantity())
                .createdBy(email)
                .updatedBy(email)
                .build();
    }

    public void updateProduct(ProductRequestDto requestDto, String updatedBy) {
        if (requestDto.getName() != null && !requestDto.getName().isEmpty()) {
            this.name = requestDto.getName();
        }
        if (requestDto.getDescription() != null && !requestDto.getDescription().isEmpty()) {
            this.description = requestDto.getDescription();
        }
        if (requestDto.getPrice() != null) {
            this.price = requestDto.getPrice();
        }
        if (requestDto.getQuantity() != null) {
            this.quantity = requestDto.getQuantity();
        }
        this.updateUpdatedBy(updatedBy);
    }

    public void softDeleteProduct(String deletedBy) {
        this.softDelete(deletedBy);
    }

    public ProductResponseDto toProductResponseDto() {
        return new ProductResponseDto(
                this.id,
                this.name,
                this.description,
                this.price,
                this.quantity,
                this.getCreatedAt(),
                this.getCreatedBy(),
                this.getUpdatedAt(),
                this.getUpdatedBy()
        );
    }
}
