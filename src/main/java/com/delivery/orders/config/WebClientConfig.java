package com.delivery.orders.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Value("${PRODUCT_API_URI:http://localhost:8001}")
    private String productApiUri;
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(productApiUri)
                .build();
    }
}
