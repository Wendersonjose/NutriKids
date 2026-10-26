package br.com.nutrikids.paciente;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.responsavel.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class PacienteService {
  private final PacienteRepository repo;
  private final AcessoPaciente acesso;
  private final VinculoRepository vinculos;
  private final Auditoria audit;

  public PacienteService(
      PacienteRepository r, AcessoPaciente a, VinculoRepository v, Auditoria au) {
    repo = r;
    acesso = a;
    vinculos = v;
    audit = au;
  }

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "PacienteVista",
      description =
          "Contrato Paciente Vista. Exemplos e identificadores exclusivamente sintéticos.")
  public record Vista(
      UUID id, String nome, LocalDate nascimento, String sexo, String observacoes, long versao) {}

  private Vista vista(Paciente p) {
    return new Vista(
        p.id,
        p.nome,
        p.nascimento,
        p.sexo,
        Identidade.atual().familia() ? null : p.observacoes,
        p.versao);
  }

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "PacienteDados",
      description =
          "Contrato Paciente Dados. Exemplos e identificadores exclusivamente sintéticos.")
  public record Dados(
      @NotBlank @Size(max = 150) String nome,
      @NotNull @PastOrPresent LocalDate nascimento,
      @Pattern(regexp = "F|M|NAO_INFORMADO") @NotNull String sexo,
      @Size(max = 4000) String observacoes) {}

  public List<Vista> listar(int page, int size, String nome) {
    var i = Identidade.atual();
    var ids =
        vinculos.findByUsuarioIdAndClinicaId(i.id(), i.clinicaId()).stream()
            .map(v -> v.pacienteId)
            .toList();
    return repo
        .findAll(
            (root, query, cb) -> {
              var filtro =
                  cb.and(
                      cb.equal(root.get("clinicaId"), i.clinicaId()),
                      cb.like(
                          cb.lower(root.get("nome")),
                          "%"
                              + nome.toLowerCase(Locale.ROOT)
                                  .replace("!", "!!")
                                  .replace("%", "!%")
                                  .replace("_", "!_")
                              + "%",
                          '!'));
              return i.familia() ? cb.and(filtro, root.get("id").in(ids)) : filtro;
            },
            Paginacao.de(page, size, "nome", "id"))
        .stream()
        .map(this::vista)
        .toList();
  }

  @Transactional
  public Vista criar(@Valid Dados d) {
    AcessoPaciente.profissional();
    var p = new Paciente();
    p.id = UUID.randomUUID();
    p.clinicaId = Identidade.atual().clinicaId();
    p.nome = d.nome();
    p.nascimento = d.nascimento();
    p.sexo = d.sexo();
    p.observacoes = d.observacoes();
    p.criadoEm = Instant.now();
    repo.save(p);
    audit.registrar("PACIENTE_CRIADO", p.id);
    return vista(p);
  }

  public Vista obter(UUID id) {
    var p = acesso.obter(id);
    audit.registrar("PACIENTE_LIDO", id);
    return vista(p);
  }

  @Transactional
  public Vista editar(UUID id, @Valid Dados d) {
    AcessoPaciente.profissional();
    var p = acesso.obter(id);
    p.nome = d.nome();
    p.nascimento = d.nascimento();
    p.sexo = d.sexo();
    p.observacoes = d.observacoes();
    audit.registrar("PACIENTE_EDITADO", id);
    return vista(repo.saveAndFlush(p));
  }
}
