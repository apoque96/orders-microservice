package com.delivery.orders.controller;

import com.delivery.orders.dto.AddProductRequest;
import com.delivery.orders.dto.OrderResponse;
import com.delivery.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders and cart operations")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping("/cart/add-product")
    @Operation(
        summary = "Add product to cart",
        description = "Adds a product to the customer's cart. If no cart exists, creates a new one with CART status."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product successfully added to cart",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Customer or product not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Product not available or insufficient stock"
        )
    })
    public Mono<ResponseEntity<OrderResponse>> addProductToCart(
            @Valid @RequestBody AddProductRequest request) {
        
        log.info("Received request to add product {} to cart for customer {}", 
                request.getProductId(), request.getCustomerId());
        
        return orderService.addProductToCart(request)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build())
                .onErrorResume(e -> e.getMessage().contains("not found"), 
                        e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(e -> e.getMessage().contains("not available") || e.getMessage().contains("Insufficient stock"),
                        e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()));
    }
}
