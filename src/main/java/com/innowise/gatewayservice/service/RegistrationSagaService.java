package com.innowise.gatewayservice.service;

import com.innowise.gatewayservice.client.AuthClient;
import com.innowise.gatewayservice.client.UserClient;
import com.innowise.gatewayservice.dto.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationSagaService {

  private final AuthClient authClient;
  private final UserClient userClient;

  public Mono<ResponseEntity<Void>> register(RegistrationRequest request) {
    return authClient.register(request.username(), request.password())
            .flatMap(authId ->
                    userClient.createUser(
                                    authId,
                                    request.name(),
                                    request.surname(),
                                    request.email(),
                                    request.birthDate()
                            )
                            .thenReturn(ResponseEntity.ok().<Void>build())
                            .onErrorResume(ex -> rollback(authId, ex))
            );
  }

  private Mono<ResponseEntity<Void>> rollback(Long authId, Throwable originalError) {
    log.error("Registration failed, starting rollback for authId={}", authId, originalError);
    return userClient.deleteUser(authId)
            .onErrorResume(e -> {
              log.error("Rollback failed: could not delete user in User-service, authId={}", authId, e);
              return Mono.empty();
            })
            .then(authClient.delete(authId)
                    .onErrorResume(e -> {
                      log.error("Rollback failed: could not delete user in Auth-service, authId={}", authId, e);
                      return Mono.empty();
                    })
            )
            .then(Mono.error(originalError));
  }
}
