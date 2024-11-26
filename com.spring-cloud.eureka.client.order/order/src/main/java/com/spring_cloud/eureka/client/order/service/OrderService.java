package com.spring_cloud.eureka.client.order.service;

import com.spring_cloud.eureka.client.order.client.product.ProductClient;
import com.spring_cloud.eureka.client.order.client.product.ProductResponseDto;
import com.spring_cloud.eureka.client.order.dto.OrderRequestDto;
import com.spring_cloud.eureka.client.order.dto.OrderResponseDto;
import com.spring_cloud.eureka.client.order.entity.Order;
import com.spring_cloud.eureka.client.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userId, int orderQuantity) {
        // Check if products exist and if they have enough quantity
        for (UUID productId : requestDto.getOrderItemIds()) {
            ProductResponseDto product = productClient.getProduct(productId);
            log.info("############################ Product 수량 확인 : " + product.getQuantity());
            if (product.getQuantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with ID " + productId + " is out of stock.");
            }
        }

        // Reduce the quantity of each product by 1
        for (UUID productId : requestDto.getOrderItemIds()) {
            productClient.reduceProductQuantity(productId, orderQuantity);
        }


        Order order = Order.createOrder(requestDto.getOrderItemIds(), userId);
        Order savedOrder = orderRepository.save(order);
        return OrderResponseDto.toOrderResponseDtoFrom(savedOrder);
    }

}
