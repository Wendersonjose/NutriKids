package br.com.nutrikids.agenda;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import br.com.nutrikids.usuario.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service
public class AgendaService {
  @io.swagger.v3.oas.annotations.media.Schema(
      name = "AgendaVista",
      description = "Contrato Agenda Vista. Exemplos e identificadores exclusivamente sintéticos.")
  public record Vista(
      UUID id,
      UUID pacienteId,
      UUID profissionalId,
      OffsetDateTime inicio,
      OffsetDateTime fim,
      String status,
      long versao) {}

  private Vista vista(Consulta c) {
    return new Vista(c.id, c.pacienteId, c.profissionalId, c.inicio, c.fim, c.status, c.versao);
  }

  private final ConsultaRepository repo;
  private final UsuarioRepository users;
  private final AcessoPaciente acesso;
  private final Auditoria audit;

  public AgendaService(ConsultaRepository r, UsuarioRepository u, AcessoPaciente a, Auditoria au) {
    repo = r;
    users = u;
    acesso = a;
    audit = au;
  }

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "AgendaDados",
      description = "Contrato Agenda Dados. Exemplos e identificadores exclusivamente sintéticos.")
  public record Dados(
      @NotNull UUID pacienteId,
      @NotNull UUID profissionalId,
      @NotNull OffsetDateTime inicio,
      @NotNull OffsetDateTime fim) {}

  public List<Vista> listar(
      int page, int size, UUID profissionalId, OffsetDateTime inicio, OffsetDateTime fim) {
    AcessoPaciente.profissional();
    return repo
        .findAll(
            (root, query, cb) -> {
              var filtros = new ArrayList<jakarta.persistence.criteria.Predicate>();
              filtros.add(cb.equal(root.get("clinicaId"), Identidade.atual().clinicaId()));
              if (profissionalId != null)
                filtros.add(cb.equal(root.get("profissionalId"), profissionalId));
              if (inicio != null) filtros.add(cb.greaterThanOrEqualTo(root.get("inicio"), inicio));
              if (fim != null) filtros.add(cb.lessThan(root.get("inicio"), fim));
              return cb.and(filtros.toArray(jakarta.persistence.criteria.Predicate[]::new));
            },
            Paginacao.de(page, size, "inicio", "id"))
        .stream()
        .map(this::vista)
        .toList();
  }

  @Transactional
  public Vista criar(@Valid Dados d) {
    AcessoPaciente.profissional();
    acesso.obter(d.pacienteId());
    var i = Identidade.atual();
    var u =
        users
            .findById(d.profissionalId())
            .filter(x -> x.clinicaId.equals(i.clinicaId()) && !x.perfil.equals("RESPONSAVEL"))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    if (!d.fim().isAfter(d.inicio())
        || Duration.between(d.inicio(), d.fim()).compareTo(Duration.ofHours(8)) > 0)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Intervalo inválido");
    var c = new Consulta();
    c.id = UUID.randomUUID();
    c.clinicaId = i.clinicaId();
    c.pacienteId = d.pacienteId();
    c.profissionalId = u.id;
    c.inicio = d.inicio();
    c.fim = d.fim();
    c.status = "AGENDADA";
    repo.saveAndFlush(c);
    audit.registrar("CONSULTA_CRIADA", c.id);
    return vista(c);
  }

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "AgendaEstado",
      description = "Contrato Agenda Estado. Exemplos e identificadores exclusivamente sintéticos.")
  public record Estado(@NotNull @Pattern(regexp = "CANCELADA|CONCLUIDA") String status) {}

  @Transactional
  public Vista estado(UUID id, @Valid Estado d) {
    AcessoPaciente.profissional();
    var c =
        repo.findByIdAndClinicaId(id, Identidade.atual().clinicaId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!c.status.equals("AGENDADA"))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Consulta já encerrada");
    c.status = d.status();
    audit.registrar("CONSULTA_" + d.status(), id);
    return vista(repo.saveAndFlush(c));
  }
}
