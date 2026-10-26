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

@org.springframework.stereotype.Service
public class EquipeService {
  @io.swagger.v3.oas.annotations.media.Schema(
      name = "EquipeVistaUsuario",
      description =
          "Contrato Equipe VistaUsuario. Exemplos e identificadores exclusivamente sintéticos.")
  public record VistaUsuario(UUID id, String nome, String email, String perfil, String crn) {}

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "EquipeVistaClinica",
      description =
          "Contrato Equipe VistaClinica. Exemplos e identificadores exclusivamente sintéticos.")
  public record VistaClinica(UUID id, String nome) {}

  private VistaUsuario vista(Usuario u) {
    return new VistaUsuario(u.id, u.nome, u.email, u.perfil, u.crn);
  }

  private final UsuarioRepository users;
  private final ClinicaRepository clinics;
  private final VinculoRepository links;
  private final AcessoPaciente acesso;
  private final PasswordEncoder encoder;
  private final Auditoria audit;

  public EquipeService(
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

  public List<VistaUsuario> listar(int page, int size) {
    AcessoPaciente.profissional();
    return users
        .findAll(
            (root, query, cb) -> cb.equal(root.get("clinicaId"), Identidade.atual().clinicaId()),
            Paginacao.de(page, size, "nome", "id"))
        .stream()
        .map(this::vista)
        .toList();
  }

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "EquipeDados",
      description = "Contrato Equipe Dados. Exemplos e identificadores exclusivamente sintéticos.")
  public record Dados(
      @NotBlank @Size(max = 150) String nome,
      @NotBlank @Email String email,
      @NotBlank
          @Size(min = 12, max = 72)
          @io.swagger.v3.oas.annotations.media.Schema(
              accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY,
              example = "SenhaSintetica123!")
          String senha,
      @NotNull @Pattern(regexp = "ADMIN|NUTRICIONISTA|RESPONSAVEL") String perfil,
      @Size(max = 30) String crn) {}

  @Transactional
  public VistaUsuario criar(@Valid Dados d) {
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
    return vista(u);
  }

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "EquipeVincular",
      description =
          "Contrato Equipe Vincular. Exemplos e identificadores exclusivamente sintéticos.")
  public record Vincular(@NotNull UUID usuarioId, @NotBlank @Size(max = 60) String parentesco) {}

  @Transactional
  public void vincular(UUID id, @Valid Vincular d) {
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

  public VistaClinica clinica() {
    AcessoPaciente.profissional();
    var c = clinics.findById(Identidade.atual().clinicaId()).orElseThrow();
    return new VistaClinica(c.id, c.nome);
  }

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "EquipeNome",
      description = "Contrato Equipe Nome. Exemplos e identificadores exclusivamente sintéticos.")
  public record Nome(@NotBlank @Size(max = 150) String nome) {}

  @Transactional
  public VistaClinica editar(@Valid Nome d) {
    AcessoPaciente.admin();
    var c = clinics.findById(Identidade.atual().clinicaId()).orElseThrow();
    c.nome = d.nome();
    audit.registrar("CLINICA_EDITADA", c.id);
    clinics.save(c);
    return new VistaClinica(c.id, c.nome);
  }
}
