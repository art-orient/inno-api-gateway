package com.innowise.gatewayservice.controller;

import com.innowise.gatewayservice.dto.RegistrationRequest;
import com.innowise.gatewayservice.service.RegistrationSagaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(RegistrationController.class)
class RegistrationControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private RegistrationSagaService sagaService;

  @Test
  void controller_callsSagaService() {
    Mockito.when(sagaService.register(any()))
            .thenReturn(Mono.just(org.springframework.http.ResponseEntity.ok().build()));

    RegistrationRequest req = new RegistrationRequest(
            "alex", "pass", "Alex", "Artsikhovich",
            "orientirik@gmail.com", LocalDate.of(1995, 1, 1)
    );

    webTestClient.post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isOk();

    Mockito.verify(sagaService).register(any());
  }
}