package br.com.nutrikids.auth;

import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Limite local por endereço de conexão. Em múltiplas instâncias, usar armazenamento compartilhado.
 */
@Component
public class ProtecaoLogin {
  private final Map<String, Janela> tentativas = new HashMap<>();

  private record Janela(Instant inicio, int quantidade) {}

  public synchronized void conferir(String origem) {
    var now = Instant.now();
    tentativas.entrySet().removeIf(e -> e.getValue().inicio().plusSeconds(900).isBefore(now));
    var j = tentativas.getOrDefault(origem, new Janela(now, 0));
    if (j.quantidade() >= 15)
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "Aguarde 15 minutos para tentar novamente");
    if (tentativas.size() >= 10000 && !tentativas.containsKey(origem))
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
    tentativas.put(origem, new Janela(j.inicio(), j.quantidade() + 1));
  }
}
