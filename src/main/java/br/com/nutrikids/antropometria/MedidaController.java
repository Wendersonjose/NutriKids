package br.com.nutrikids.antropometria;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes/{pacienteId}/medidas")
public class MedidaController {
  private final MedidaRepository repo;
  private final AcessoPaciente acesso;
  private final Auditoria audit;

  public MedidaController(MedidaRepository r, AcessoPaciente a, Auditoria au) {
    repo = r;
    acesso = a;
    audit = au;
  }

  record Dados(
      @NotNull @PastOrPresent LocalDate data,
      @NotNull @DecimalMin("0.1") @DecimalMax("400") BigDecimal pesoKg,
      @NotNull @DecimalMin("20") @DecimalMax("250") BigDecimal alturaCm,
      @NotNull @Pattern(regexp = "DEITADO|EM_PE") String posicao) {}

  @GetMapping
  List<Medida> listar(@PathVariable UUID pacienteId) {
    acesso.obter(pacienteId);
    audit.registrar("MEDIDAS_LIDAS", pacienteId);
    return repo.findByPacienteIdAndClinicaIdOrderByDataAsc(
        pacienteId, Identidade.atual().clinicaId());
  }

  @PostMapping
  @Transactional
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  Medida criar(@PathVariable UUID pacienteId, @Valid @RequestBody Dados d) {
    AcessoPaciente.profissional();
    var p = acesso.obter(pacienteId);
    if (d.data().isBefore(p.nascimento))
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "Medida anterior ao nascimento");
    var i = Identidade.atual();
    var m = new Medida();
    m.id = UUID.randomUUID();
    m.clinicaId = i.clinicaId();
    m.pacienteId = pacienteId;
    m.autorId = i.id();
    m.data = d.data();
    m.pesoKg = d.pesoKg();
    m.alturaCm = d.alturaCm();
    m.posicao = d.posicao();
    repo.save(m);
    audit.registrar("MEDIDA_CRIADA", m.id);
    return m;
  }
}
