package com.innowise.gatewayservice.client;

import com.innowise.gatewayservice.dto.AuthUserDto;
import com.innowise.gatewayservice.dto.auth.RegisterPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthClient {

  @Value("${services.auth.url}")
  private String authUrl;

  private final WebClient webClient;

  public Mono<Long> register(String username, String password) {
    return webClient.post()
            .uri(authUrl + "/api/auth/credentials")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new RegisterPayload(username, password))
            .retrieve()
            .bodyToMono(AuthUserDto.class)
            .map(dto -> {
              if (dto == null || dto.id() == null) {
                throw new IllegalStateException("Auth-service did not return user id");
              }
              return dto.id();
            });
  }

  public Mono<Void> delete(Long id) {
    return webClient.delete()
            .uri(authUrl + "/api/auth/{id}", id)
            .retrieve()
            .toBodilessEntity()
            .then();
  }
}