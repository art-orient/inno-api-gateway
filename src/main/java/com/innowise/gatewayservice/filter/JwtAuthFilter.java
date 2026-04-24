package com.innowise.gatewayservice.filter;

import com.innowise.gatewayservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered {

  private final JwtUtil jwtUtil;
  private static final AntPathMatcher matcher = new AntPathMatcher();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    if (isPublicPath(path)) {
      return chain.filter(exchange);
    }

    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return unauthorized(exchange);
    }
    log.warn("AUTH HEADER = {}", exchange.getRequest().getHeaders().getFirst("Authorization"));
    log.info("JwtAuthFilter: path={}, Authorization={}", path, authHeader);
    String token = authHeader.substring(7);
    log.info("JwtAuthFilter: token='{}'", token);
    if (!jwtUtil.isValid(token)) {
      return unauthorized(exchange);
    }
    String userId = jwtUtil.getUserId(token);
    ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-User-Id", userId)
            .header("Authorization", authHeader)
            .build();
    return chain.filter(exchange.mutate().request(mutatedRequest).build());
  }

  private boolean isPublicPath(String path) {
    return matcher.match("/api/auth/login", path)
            || matcher.match("/api/auth/credentials", path)
            || matcher.match("/api/registrations", path);
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }

  @Override
  public int getOrder() {
    return -1;
  }
}