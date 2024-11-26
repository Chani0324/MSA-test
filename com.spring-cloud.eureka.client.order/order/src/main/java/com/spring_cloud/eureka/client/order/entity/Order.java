package com.spring_cloud.eureka.client.order.entity;

import com.spring_cloud.eureka.client.order.dto.OrderResponseDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "order_item_id")
    private List<UUID> orderItemIds;

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    // 팩토리 메서드
    public static Order createOrder(List<UUID> orderItemIds, String createdBy) {
        return Order.builder()
                .orderItemIds(orderItemIds)
                .createdBy(createdBy)
                .status(OrderStatus.CREATED)
                .build();
    }

    // 업데이트 메서드
    public void updateOrder(List<UUID> orderItemIds, String updatedBy, OrderStatus status) {
        this.orderItemIds = orderItemIds;
        this.updateUpdatedBy(updatedBy);
        this.status = status;
    }

    public void softDeleteOrder(String deletedBy) {
        this.softDelete(deletedBy);
    }

    public OrderResponseDto toResponseDto() {
        return new OrderResponseDto(
                this.orderId,
                this.status,
                this.getCreatedAt(),
                this.getCreatedBy(),
                this.getUpdatedAt(),
                this.getUpdatedBy(),
                this.orderItemIds
        );
    }
}
