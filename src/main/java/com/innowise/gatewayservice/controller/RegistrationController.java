package com.innowise.gatewayservice.controller;

import com.innowise.gatewayservice.dto.RegistrationRequest;
import com.innowise.gatewayservice.service.RegistrationSagaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

  private final RegistrationSagaService sagaService;

  @PostMapping
  public Mono<ResponseEntity<Void>> register(@RequestBody RegistrationRequest request) {
    return sagaService.register(request)
            .onErrorResume(WebClientResponseException.BadRequest.class, ex ->
                    Mono.just(ResponseEntity.badRequest().build())
            );
  }
}