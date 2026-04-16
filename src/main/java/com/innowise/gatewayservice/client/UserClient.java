package com.innowise.gatewayservice.client;

import com.innowise.gatewayservice.dto.UserCreatePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class UserClient {

  @Value("${services.user.url}")
  private String userUrl;

  private final WebClient webClient;

  public Mono<Void> createUser(Long id,
                               String name,
                               String surname,
                               String email,
                               LocalDate birthDate) {

    UserCreatePayload payload =
            new UserCreatePayload(id, name, surname, email, true, birthDate);

    return webClient.post()
            .uri(userUrl + "/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .toBodilessEntity()
            .then();
  }

  public Mono<Void> deleteUser(Long id) {
    return webClient.delete()
            .uri(userUrl + "/api/users/{id}", id)
            .retrieve()
            .toBodilessEntity()
            .then();
  }
}