package com.qpic.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Single place where JWTs are verified.
 *  - strips any client-supplied X-User-Id / X-Internal-Secret (anti-spoofing)
 *  - lets public paths through
 *  - for everything else validates the access token and forwards X-User-Id to downstream services
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    static final String USER_ID_HEADER = "X-User-Id";

    private static final List<String> PUBLIC_EXACT = List.of(
            "/api/auth/register", "/api/auth/login", "/api/auth/refresh");
    private static final List<String> PUBLIC_PREFIX = List.of(
            "/api/public/", "/actuator/health");

    private final SecretKey key;

    public JwtAuthFilter(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (path.startsWith("/internal")) {
            return reject(exchange, HttpStatus.NOT_FOUND, "NOT_FOUND", "Not found");
        }

        ServerHttpRequest.Builder builder = request.mutate().headers(h -> {
            h.remove(USER_ID_HEADER);
            h.remove("X-Internal-Secret");
        });

        if (HttpMethod.OPTIONS.equals(request.getMethod()) || isPublic(path)) {
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing bearer token");
        }
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(header.substring(7).trim()).getPayload();
            if (!"access".equals(claims.get("typ", String.class))) {
                return reject(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Not an access token");
            }
            builder.header(USER_ID_HEADER, claims.getSubject());
            return chain.filter(exchange.mutate().request(builder.build()).build());
        } catch (JwtException | IllegalArgumentException e) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "Invalid or expired token");
        }
    }

    private boolean isPublic(String path) {
        return PUBLIC_EXACT.contains(path) || PUBLIC_PREFIX.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":" + status.value() + ",\"code\":\"" + code + "\",\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
