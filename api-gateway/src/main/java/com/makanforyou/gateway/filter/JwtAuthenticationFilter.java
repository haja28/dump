package com.makanforyou.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT Authentication filter for API Gateway
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = new ArrayList<>();

    static {
        PUBLIC_ENDPOINTS.add("/api/v1/auth/register");
        PUBLIC_ENDPOINTS.add("/api/v1/auth/login");
        PUBLIC_ENDPOINTS.add("/api/v1/auth/refresh");
        PUBLIC_ENDPOINTS.add("/api/v1/kitchens");
        PUBLIC_ENDPOINTS.add("/api/v1/menu-items/search");
        PUBLIC_ENDPOINTS.add("/api/v1/images");
        PUBLIC_ENDPOINTS.add("/v3/api-docs");
        PUBLIC_ENDPOINTS.add("/swagger-ui");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        // Extract token
        String token = authHeader.substring(7);

        try {
            // Validate token
            Claims claims = validateToken(token);

            // Add user info to request headers
            Long userId = claims.get("userId", Long.class);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId.toString())
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception ex) {
            log.error("JWT validation error: {}", ex.getMessage());
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
    }

    private Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new RuntimeException("JWT secret is not configured");
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"success\": false, \"message\": \"%s\"}", message);
        DataBuffer dataBuffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(dataBuffer));
    }
}
