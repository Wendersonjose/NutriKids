package br.com.nutrikids.prontuario;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes/{pacienteId}/registros")
public class RegistroController {
  private final RegistroRepository repo;
  private final AcessoPaciente acesso;
  private final Auditoria audit;

  public RegistroController(RegistroRepository r, AcessoPaciente a, Auditoria au) {
    repo = r;
    acesso = a;
    audit = au;
  }

  record Dados(
      @NotNull
          @Pattern(
              regexp =
                  "PRONTUARIO|ANAMNESE|PLANO_ALIMENTAR|DIARIO_ALIMENTAR|TERAPIA_ALIMENTAR|MENSAGEM|META")
          String tipo,
      @NotBlank @Size(max = 12000) String conteudo,
      boolean compartilhado) {}

  @GetMapping
  List<Registro> listar(@PathVariable UUID pacienteId) {
    acesso.obter(pacienteId);
    audit.registrar("HISTORICO_LIDO", pacienteId);
    var i = Identidade.atual();
    return repo.findByPacienteIdAndClinicaIdOrderByCriadoEmDesc(pacienteId, i.clinicaId()).stream()
        .filter(r -> !i.familia() || r.compartilhado)
        .toList();
  }

  @PostMapping
  @Transactional
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  Registro criar(@PathVariable UUID pacienteId, @Valid @RequestBody Dados d) {
    acesso.obter(pacienteId);
    var i = Identidade.atual();
    if (i.familia() && !Set.of("DIARIO_ALIMENTAR", "MENSAGEM").contains(d.tipo()))
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.FORBIDDEN);
    var r = new Registro();
    r.id = UUID.randomUUID();
    r.clinicaId = i.clinicaId();
    r.pacienteId = pacienteId;
    r.autorId = i.id();
    r.tipo = d.tipo();
    r.conteudo = d.conteudo();
    r.compartilhado = i.familia() || d.compartilhado();
    r.criadoEm = Instant.now();
    repo.save(r);
    audit.registrar("REGISTRO_" + d.tipo(), r.id);
    return r;
  }
}
