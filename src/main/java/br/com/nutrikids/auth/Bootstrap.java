package br.com.nutrikids.auth;

import br.com.nutrikids.clinica.*;
import br.com.nutrikids.usuario.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Bootstrap implements ApplicationRunner {
  private final UsuarioRepository users;
  private final ClinicaRepository clinics;
  private final PasswordEncoder encoder;

  @Value("${BOOTSTRAP_EMAIL:}")
  String email;

  @Value("${BOOTSTRAP_PASSWORD:}")
  String password;

  public Bootstrap(UsuarioRepository u, ClinicaRepository c, PasswordEncoder e) {
    users = u;
    clinics = c;
    encoder = e;
  }

  @Transactional
  public void run(ApplicationArguments args) {
    if (users.count() > 0 || email.isBlank()) return;
    if (password.length() < 12
        || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException("BOOTSTRAP_PASSWORD exige 12 caracteres");
    var c = new Clinica(UUID.randomUUID(), "Minha clínica");
    clinics.save(c);
    var u = new Usuario();
    u.id = UUID.randomUUID();
    u.clinicaId = c.id;
    u.nome = "Administrador";
    u.email = email.toLowerCase(Locale.ROOT);
    u.senha = encoder.encode(password);
    u.perfil = "ADMIN";
    users.save(u);
  }
}
