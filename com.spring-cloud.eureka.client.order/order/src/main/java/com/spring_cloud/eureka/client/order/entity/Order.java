package com.spring_cloud.eureka.client.order.entity;

import com.spring_cloud.eureka.client.order.dto.OrderProductListDto;
import com.spring_cloud.eureka.client.order.dto.OrderResponseDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "order_product_id")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderProductList> orderProductList = new ArrayList<>();

    public void addOrderProduct(OrderProductList orderProductList) {
        this.orderProductList.add(orderProductList);
        orderProductList.updateOrder(this);
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    // 팩토리 메서드
    public static Order createOrderFrom(String createdBy) {
        return Order.builder()
                .orderProductList(new ArrayList<>())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .status(OrderStatus.CREATED)
                .build();
    }

    // 업데이트 메서드
    public void updateOrder(List<OrderProductList> orderProductList, String updatedBy, OrderStatus status) {
        this.orderProductList = orderProductList;
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
                this.orderProductList.stream()
                .map(OrderProductListDto::fromOrderProductList)
                .toList()
        );
    }

    public static Order fromResponseDto(OrderResponseDto orderResponseDto) {
        return Order.builder()
                .orderId(orderResponseDto.getOrderId())
                .status(orderResponseDto.getStatus())
                .createdAt(orderResponseDto.getCreatedAt())
                .createdBy(orderResponseDto.getCreatedBy())
                .updatedAt(orderResponseDto.getUpdatedAt())
                .updatedBy(orderResponseDto.getUpdatedBy())
                // Builder패턴 쓸 때 주의해야하는 부분. list초기화 안해주면 진짜 큰일남...
                .orderProductList(new ArrayList<>())
                .build();
    }
}
