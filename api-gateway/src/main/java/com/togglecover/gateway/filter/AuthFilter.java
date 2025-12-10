package com.togglecover.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    public AuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Skip auth for public endpoints
            String path = exchange.getRequest().getURI().getPath();
            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            // Get token from header
            String authHeader = exchange.getRequest().getHeaders()
                    .getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            // Validate token with Auth Service
            return webClientBuilder.build()
                    .post()
                    .uri("lb://AUTH-SERVICE/auth/api/auth/validate")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .flatMap(response -> chain.filter(exchange))
                    .onErrorResume(e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
                path.contains("/health") ||
                path.contains("/actuator") ||
                path.contains("/swagger") ||
                path.contains("/api-docs") ||
                path.contains("/h2-console");
    }

    public static class Config {
        // Configuration properties if needed
    }
}