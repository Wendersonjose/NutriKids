package br.com.nutrikids.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ProtecaoLoginTest {
  @Test
  void limitaTentativasPorOrigem() {
    var l = new ProtecaoLogin();
    for (int n = 0; n < 15; n++) l.conferir("origem");
    var e = assertThrows(ResponseStatusException.class, () -> l.conferir("origem"));
    assertEquals(429, e.getStatusCode().value());
    assertDoesNotThrow(() -> l.conferir("outra-origem"));
  }
}
