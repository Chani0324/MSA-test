package com.spring_cloud.eureka.client.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class OrderProductList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderListId;

    private UUID productId;
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private Order order;

    public void updateOrder(Order order) {
        this.order = order;
    }
}
