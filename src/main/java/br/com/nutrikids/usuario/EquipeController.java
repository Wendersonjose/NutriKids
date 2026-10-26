package br.com.nutrikids.usuario;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.clinica.*;
import br.com.nutrikids.paciente.*;
import br.com.nutrikids.responsavel.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EquipeController {
  private final UsuarioRepository users;
  private final ClinicaRepository clinics;
  private final VinculoRepository links;
  private final AcessoPaciente acesso;
  private final PasswordEncoder encoder;
  private final Auditoria audit;

  public EquipeController(
      UsuarioRepository u,
      ClinicaRepository c,
      VinculoRepository l,
      AcessoPaciente a,
      PasswordEncoder e,
      Auditoria au) {
    users = u;
    clinics = c;
    links = l;
    acesso = a;
    encoder = e;
    audit = au;
  }

  @GetMapping("/equipe")
  List<Usuario> listar() {
    AcessoPaciente.profissional();
    return users.findByClinicaId(Identidade.atual().clinicaId());
  }

  record Dados(
      @NotBlank @Size(max = 150) String nome,
      @NotBlank @Email String email,
      @NotBlank @Size(min = 12, max = 72) String senha,
      @NotNull @Pattern(regexp = "ADMIN|NUTRICIONISTA|RESPONSAVEL") String perfil,
      @Size(max = 30) String crn) {}

  @PostMapping("/equipe")
  @Transactional
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  Usuario criar(@Valid @RequestBody Dados d) {
    AcessoPaciente.admin();
    if (d.senha().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "Senha excede 72 bytes UTF-8");
    var u = new Usuario();
    u.id = UUID.randomUUID();
    u.clinicaId = Identidade.atual().clinicaId();
    u.nome = d.nome();
    u.email = d.email().toLowerCase(Locale.ROOT);
    u.senha = encoder.encode(d.senha());
    u.perfil = d.perfil();
    u.crn = d.crn();
    users.save(u);
    audit.registrar("USUARIO_CRIADO", u.id);
    return u;
  }

  record Vincular(@NotNull UUID usuarioId, @NotBlank @Size(max = 60) String parentesco) {}

  @PostMapping("/pacientes/{id}/responsaveis")
  @Transactional
  void vincular(@PathVariable UUID id, @Valid @RequestBody Vincular d) {
    AcessoPaciente.admin();
    acesso.obter(id);
    var i = Identidade.atual();
    users
        .findById(d.usuarioId())
        .filter(u -> u.clinicaId.equals(i.clinicaId()) && u.perfil.equals("RESPONSAVEL"))
        .orElseThrow(
            () ->
                new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST));
    var v = new Vinculo();
    v.id = UUID.randomUUID();
    v.clinicaId = i.clinicaId();
    v.pacienteId = id;
    v.usuarioId = d.usuarioId();
    v.parentesco = d.parentesco();
    links.save(v);
    audit.registrar("RESPONSAVEL_VINCULADO", id);
  }

  @GetMapping("/clinica")
  Clinica clinica() {
    AcessoPaciente.profissional();
    return clinics.findById(Identidade.atual().clinicaId()).orElseThrow();
  }

  record Nome(@NotBlank @Size(max = 150) String nome) {}

  @PutMapping("/clinica")
  @Transactional
  Clinica editar(@Valid @RequestBody Nome d) {
    AcessoPaciente.admin();
    var c = clinica();
    c.nome = d.nome();
    audit.registrar("CLINICA_EDITADA", c.id);
    return clinics.save(c);
  }
}
