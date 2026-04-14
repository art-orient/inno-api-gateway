package com.innowise.gatewayservice.filter;

import com.innowise.gatewayservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global JWT authentication filter for Spring Cloud Gateway.
 * <p>
 * This filter intercepts all incoming requests and:
 * <ul>
 *   <li>allows access to public authentication endpoints</li>
 *   <li>extracts and validates the JWT token from the Authorization header</li>
 *   <li>rejects unauthorized requests with HTTP 401</li>
 *   <li>adds the authenticated user's ID to the request headers (X-User-Id)</li>
 * </ul>
 * The filter ensures that downstream microservices receive only verified requests
 * and do not perform JWT validation themselves.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

  private final JwtUtil jwtUtil;

  /**
   * Applies JWT validation logic to each incoming request.
   *
   * @param exchange the current server exchange
   * @param chain    the filter chain to delegate to
   * @return a completion signal when request processing is finished
   */
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

    String token = authHeader.substring(7);
    if (!jwtUtil.isValid(token)) {
      return unauthorized(exchange);
    }

    String userId = jwtUtil.getUserId(token);
    ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-User-Id", userId)
            .build();

    return chain.filter(exchange.mutate().request(mutatedRequest).build());
  }

  /**
   * Checks whether the request path belongs to public authentication endpoints.
   *
   * @param path the request path
   * @return true if the path does not require authentication
   */
  private boolean isPublicPath(String path) {
    return path.startsWith("/api/auth/login")
            || path.startsWith("/api/auth/register")
            || path.startsWith("/api/auth/refresh")
            || path.startsWith("/api/auth/validate");
  }

  /**
   * Returns an HTTP 401 Unauthorized response.
   *
   * @param exchange the current server exchange
   * @return a completion signal after the response is written
   */
  private Mono<Void> unauthorized(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }

  /**
   * Defines filter execution order.
   *
   * @return filter priority (lower value = higher priority)
   */
  @Override
  public int getOrder() {
    return -1;
  }
}