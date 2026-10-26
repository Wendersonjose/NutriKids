package br.com.nutrikids.paciente;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.responsavel.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {
  private final PacienteRepository repo;
  private final AcessoPaciente acesso;
  private final VinculoRepository vinculos;
  private final Auditoria audit;

  public PacienteController(
      PacienteRepository r, AcessoPaciente a, VinculoRepository v, Auditoria au) {
    repo = r;
    acesso = a;
    vinculos = v;
    audit = au;
  }

  record Vista(
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

  record Dados(
      @NotBlank @Size(max = 150) String nome,
      @NotNull @PastOrPresent LocalDate nascimento,
      @Pattern(regexp = "F|M|NAO_INFORMADO") @NotNull String sexo,
      @Size(max = 4000) String observacoes) {}

  @GetMapping
  List<Vista> listar() {
    var i = Identidade.atual();
    var all = repo.findByClinicaIdOrderByNome(i.clinicaId());
    if (!i.familia()) return all.stream().map(this::vista).toList();
    var ids =
        vinculos.findByUsuarioIdAndClinicaId(i.id(), i.clinicaId()).stream()
            .map(v -> v.pacienteId)
            .toList();
    return all.stream().filter(p -> ids.contains(p.id)).map(this::vista).toList();
  }

  @PostMapping
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  @Transactional
  Paciente criar(@Valid @RequestBody Dados d) {
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
    return p;
  }

  @GetMapping("/{id}")
  Vista obter(@PathVariable UUID id) {
    var p = acesso.obter(id);
    audit.registrar("PACIENTE_LIDO", id);
    return vista(p);
  }

  @PutMapping("/{id}")
  @Transactional
  Paciente editar(@PathVariable UUID id, @Valid @RequestBody Dados d) {
    AcessoPaciente.profissional();
    var p = acesso.obter(id);
    p.nome = d.nome();
    p.nascimento = d.nascimento();
    p.sexo = d.sexo();
    p.observacoes = d.observacoes();
    audit.registrar("PACIENTE_EDITADO", id);
    return repo.save(p);
  }
}
