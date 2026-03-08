package com.delivery.orders.service;

import com.delivery.orders.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final WebClient webClient;
    
    public Mono<ProductResponse> getProductById(Integer productId) {
        log.info("Fetching product with ID: {}", productId);
        
        return webClient.get()
                .uri("/api/v1/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .doOnSuccess(product -> log.info("Successfully fetched product: {}", product.getName()))
                .doOnError(error -> log.error("Error fetching product with ID {}: {}", productId, error.getMessage()));
    }
}
