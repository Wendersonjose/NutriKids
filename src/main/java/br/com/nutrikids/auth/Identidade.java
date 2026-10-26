package br.com.nutrikids.auth;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

public record Identidade(UUID id, UUID clinicaId, String perfil, String nome) {
  public static Identidade atual() {
    return (Identidade) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public boolean familia() {
    return perfil.equals("RESPONSAVEL");
  }
}
