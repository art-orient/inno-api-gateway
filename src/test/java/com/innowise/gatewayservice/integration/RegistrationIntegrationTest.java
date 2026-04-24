package com.innowise.gatewayservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.gatewayservice.dto.RegistrationRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RegistrationIntegrationTest {

  static MockWebServer authServer;
  static MockWebServer userServer;

  @BeforeAll
  static void startServers() throws IOException {
    authServer = new MockWebServer();
    userServer = new MockWebServer();

    // обычный старт — но мы НЕ будем использовать authServer.url()
    authServer.start();
    userServer.start();
  }

  @AfterAll
  static void stopServers() throws IOException {
    authServer.shutdown();
    userServer.shutdown();
  }

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry registry) {
    // НЕ используем authServer.url("/") — он даёт kubernetes.docker.internal
    registry.add("MOCK_AUTH_URL", () -> "http://localhost:" + authServer.getPort());
    registry.add("MOCK_USER_URL", () -> "http://localhost:" + userServer.getPort());
  }

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ObjectMapper objectMapper;

  private RegistrationRequest req() {
    return new RegistrationRequest(
            "alex",
            "pass",
            "Alex",
            "Artsikhovich",
            "orientirik@gmail.com",
            LocalDate.of(1995, 1, 1)
    );
  }

  @Test
  void register_success() throws Exception {
    authServer.enqueue(new MockResponse()
            .setResponseCode(201)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"id\": 10}"));

    userServer.enqueue(new MockResponse()
            .setResponseCode(201)
            .setHeader("Content-Type", "application/json"));

    webTestClient.post()
            .uri("/api/registrations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(req()))
            .exchange()
            .expectStatus().isOk();
  }
}