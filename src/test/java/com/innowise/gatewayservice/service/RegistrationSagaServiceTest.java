package com.innowise.gatewayservice.service;

import com.innowise.gatewayservice.client.AuthClient;
import com.innowise.gatewayservice.client.UserClient;
import com.innowise.gatewayservice.dto.RegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationSagaServiceTest {

  private AuthClient authClient;
  private UserClient userClient;
  private RegistrationSagaService sagaService;

  @BeforeEach
  void setup() {
    authClient = mock(AuthClient.class);
    userClient = mock(UserClient.class);
    sagaService = new RegistrationSagaService(authClient, userClient);
  }

  private RegistrationRequest request() {
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
  void register_success() {
    when(authClient.register("alex", "pass"))
            .thenReturn(Mono.just(10L));
    when(userClient.createUser(
            any(), any(), any(), any(), any()
    )).thenReturn(Mono.empty());
    RegistrationRequest req = request();
    ResponseEntity<Void> response = executeRegister(req);
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    InOrder order = Mockito.inOrder(authClient, userClient);
    order.verify(authClient).register("alex", "pass");
    order.verify(userClient).createUser(10L, "Alex", "Artsikhovich", "orientirik@gmail.com",
            LocalDate.of(1995, 1, 1));
    order.verifyNoMoreInteractions();
  }

  @Test
  void register_userServiceFails_rollbackBoth() {
    RegistrationRequest req = request();
    when(authClient.register("alex", "pass"))
            .thenReturn(Mono.just(20L));
    when(userClient.createUser(any(), any(), any(), any(), any()))
            .thenReturn(Mono.error(new RuntimeException("User error")));
    when(userClient.deleteUser(20L))
            .thenReturn(Mono.empty());
    when(authClient.delete(20L))
            .thenReturn(Mono.empty());
    assertThrows(RuntimeException.class, () -> executeRegister(req));
    verify(userClient).deleteUser(20L);
    verify(authClient).delete(20L);
  }

  @Test
  void register_authServiceFails_noRollback() {
    when(authClient.register("alex", "pass"))
            .thenReturn(Mono.error(new RuntimeException("Auth error")));
    RegistrationRequest req = request();
    assertThrows(RuntimeException.class, () -> executeRegister(req));
    verifyNoInteractions(userClient);
    verify(authClient, never()).delete(any());
  }

  @Test
  void register_userRollbackFails_authRollbackStillRuns() {
    when(authClient.register("alex", "pass"))
            .thenReturn(Mono.just(30L));
    when(userClient.createUser(any(), any(), any(), any(), any()))
            .thenReturn(Mono.error(new RuntimeException("User create fail")));
    when(userClient.deleteUser(30L))
            .thenReturn(Mono.error(new RuntimeException("User rollback fail")));
    when(authClient.delete(30L))
            .thenReturn(Mono.empty());
    RegistrationRequest req = request();
    assertThrows(RuntimeException.class, () -> executeRegister(req));
    verify(userClient).deleteUser(30L);
    verify(authClient).delete(30L);
  }

  @Test
  void register_authRollbackFails_userRollbackStillRuns() {
    when(authClient.register("alex", "pass"))
            .thenReturn(Mono.just(40L));
    when(userClient.createUser(any(), any(), any(), any(), any()))
            .thenReturn(Mono.error(new RuntimeException("User create fail")));
    when(userClient.deleteUser(40L))
            .thenReturn(Mono.empty());
    when(authClient.delete(40L))
            .thenReturn(Mono.error(new RuntimeException("Auth rollback fail")));
    RegistrationRequest req = request();
    assertThrows(RuntimeException.class, () -> executeRegister(req));
    verify(userClient).deleteUser(40L);
    verify(authClient).delete(40L);
  }

  private ResponseEntity<Void> executeRegister(RegistrationRequest req) {
    return sagaService.register(req).block();
  }
}