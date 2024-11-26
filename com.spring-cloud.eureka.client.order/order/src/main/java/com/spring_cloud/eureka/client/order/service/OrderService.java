package com.spring_cloud.eureka.client.order.service;

import com.spring_cloud.eureka.client.order.client.product.ProductClient;
import com.spring_cloud.eureka.client.order.client.product.ProductResponseDto;
import com.spring_cloud.eureka.client.order.dto.OrderProductListDto;
import com.spring_cloud.eureka.client.order.dto.OrderRequestDto;
import com.spring_cloud.eureka.client.order.dto.OrderResponseDto;
import com.spring_cloud.eureka.client.order.dto.OrderSearchDto;
import com.spring_cloud.eureka.client.order.entity.Order;
import com.spring_cloud.eureka.client.order.entity.OrderProductList;
import com.spring_cloud.eureka.client.order.entity.OrderStatus;
import com.spring_cloud.eureka.client.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    /**
     * 나중에 schedulerTask 를 이용해서 주문 취소가 없을 시 5분마나 넣는다던지, 결제를 확인 후 넣는다던지.. 등등
     * 다양한 메소드 필요할듯? 주문이 진짜 개어렵...
     */
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userId) {
        // Check if products exist and if they have enough quantity
        for (OrderProductListDto orderProductList : requestDto.getOrderList()) {
            ProductResponseDto product = productClient.getProduct(orderProductList.getProductId());
            log.info("############################ Product 수량 확인 : " + product.getQuantity());
            if (product.getQuantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with ID " + orderProductList.getProductId() + " is out of stock.");
            }
        }

        Order order = Order.createOrderFrom(userId);

        // Reduce the quantity of each product, add orderProduct in order
        for (OrderProductListDto orderProductList : requestDto.getOrderList()) {
            productClient.reduceProductQuantity(orderProductList.getProductId(), orderProductList.getQuantity());

            OrderProductList toOrderProductList = getOrderProductList(orderProductList);
            order.addOrderProduct(toOrderProductList);
        }

        Order savedOrder = orderRepository.save(order);
        return OrderResponseDto.toOrderResponseDtoFrom(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrders(OrderSearchDto searchDto, Pageable pageable, String role, String userId) {
        return orderRepository.searchOrders(searchDto, pageable,role, userId);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID orderId) {
        Order order = orderRepository.findByOrderIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or has been deleted"));
        return OrderResponseDto.toOrderResponseDtoFrom(order);
    }

    // 주문 내에 있는 주문한 상품을 개별로 업데이트 할 수 있는 메서드를 만들어야 할듯...?
    @Transactional
    public OrderResponseDto updateOrder(UUID orderId, OrderRequestDto requestDto,String userId) {
        Order order = orderRepository.findByOrderIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or has been deleted"));

        List<OrderProductList> orderProductLists = new ArrayList<>();

        for (OrderProductListDto orderProductList : requestDto.getOrderList()) {
            OrderProductList toOrderProductList = getOrderProductList(orderProductList);
            orderProductLists.add(toOrderProductList);
        }

        order.updateOrder(orderProductLists, userId, OrderStatus.valueOf(requestDto.getStatus()));
        Order updatedOrder = orderRepository.save(order);

        return OrderResponseDto.toOrderResponseDtoFrom(updatedOrder);
    }

    @Transactional
    public void deleteOrder(UUID orderId, String deletedBy) {
        Order order = orderRepository.findByOrderIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or has been deleted"));
        order.softDelete(deletedBy);
        orderRepository.save(order);
    }

    private OrderProductList getOrderProductList(OrderProductListDto orderProductList) {
        return OrderProductList.builder()
                .productId(orderProductList.getProductId())
                .quantity(orderProductList.getQuantity())
                .build();
    }

}
