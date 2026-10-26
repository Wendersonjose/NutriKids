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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/agenda")
public class AgendaController {
  private final ConsultaRepository repo;
  private final UsuarioRepository users;
  private final AcessoPaciente acesso;
  private final Auditoria audit;

  public AgendaController(
      ConsultaRepository r, UsuarioRepository u, AcessoPaciente a, Auditoria au) {
    repo = r;
    users = u;
    acesso = a;
    audit = au;
  }

  record Dados(
      @NotNull UUID pacienteId,
      @NotNull UUID profissionalId,
      @NotNull OffsetDateTime inicio,
      @NotNull OffsetDateTime fim) {}

  @GetMapping
  List<Consulta> listar() {
    AcessoPaciente.profissional();
    return repo.findByClinicaIdOrderByInicioAsc(Identidade.atual().clinicaId());
  }

  @PostMapping
  @Transactional
  @ResponseStatus(HttpStatus.CREATED)
  Consulta criar(@Valid @RequestBody Dados d) {
    AcessoPaciente.profissional();
    acesso.obter(d.pacienteId());
    var i = Identidade.atual();
    var u =
        users
            .findById(d.profissionalId())
            .filter(x -> x.clinicaId.equals(i.clinicaId()) && !x.perfil.equals("RESPONSAVEL"))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    if (!d.fim().isAfter(d.inicio()) || Duration.between(d.inicio(), d.fim()).toHours() > 8)
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
    return c;
  }

  record Estado(@NotNull @Pattern(regexp = "CANCELADA|CONCLUIDA") String status) {}

  @PatchMapping("/{id}")
  @Transactional
  Consulta estado(@PathVariable UUID id, @Valid @RequestBody Estado d) {
    AcessoPaciente.profissional();
    var c =
        repo.findByIdAndClinicaId(id, Identidade.atual().clinicaId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!c.status.equals("AGENDADA"))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Consulta já encerrada");
    c.status = d.status();
    audit.registrar("CONSULTA_" + d.status(), id);
    return repo.save(c);
  }
}
