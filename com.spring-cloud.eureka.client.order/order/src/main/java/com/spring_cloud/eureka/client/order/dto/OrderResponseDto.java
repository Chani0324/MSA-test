package com.spring_cloud.eureka.client.order.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.spring_cloud.eureka.client.order.client.product.ProductResponseDto;
import com.spring_cloud.eureka.client.order.entity.Order;
import com.spring_cloud.eureka.client.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class OrderResponseDto {
    private UUID orderId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private List<UUID> orderItemIds;

    @QueryProjection
    public OrderResponseDto(UUID orderId, OrderStatus status, LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt, String updatedBy, List<UUID> orderItemIds) {
        this.orderId = orderId;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.orderItemIds = orderItemIds;
    }

    public static OrderResponseDto toOrderResponseDtoFrom(Order order) {
        return new OrderResponseDto(
                order.getOrderId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getCreatedBy(),
                order.getUpdatedAt(),
                order.getUpdatedBy(),
                order.getOrderItemIds()
        );
    }
}
