package com.spring_cloud.eureka.client.order.client.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{productId}")
    ProductResponseDto getProduct(@PathVariable("productId") UUID productId);

    @GetMapping("/products/{productId}/reduceQuantity")
    void reduceProductQuantity(@PathVariable("productId") UUID productId, @RequestParam("quantity") int quantity);
}
