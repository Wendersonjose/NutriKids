package br.com.nutrikids.antropometria;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes/{pacienteId}/crescimento")
public class OmsController {
  private final AcessoPaciente acesso;
  private final MedidaRepository repo;
  private final OmsService oms;
  private final Auditoria audit;

  public OmsController(AcessoPaciente a, MedidaRepository r, OmsService o, Auditoria au) {
    acesso = a;
    repo = r;
    oms = o;
    audit = au;
  }

  @GetMapping
  public List<?> historico(@PathVariable UUID pacienteId, @RequestParam boolean edema) {
    var p = acesso.obter(pacienteId);
    audit.registrar("CRESCIMENTO_LIDO", pacienteId);
    return repo
        .findByPacienteIdAndClinicaIdOrderByDataAsc(pacienteId, Identidade.atual().clinicaId())
        .stream()
        .map(
            m ->
                Map.of(
                    "medidaId",
                    m.id,
                    "data",
                    m.data,
                    "resultados",
                    List.of("wei", "len", "bmi", "wfl").stream()
                        .map(
                            i ->
                                oms.calcular(
                                    i,
                                    p.sexo,
                                    ChronoUnit.DAYS.between(p.nascimento, m.data),
                                    m.pesoKg.doubleValue(),
                                    m.alturaCm.doubleValue(),
                                    m.posicao,
                                    edema))
                        .toList()))
        .toList();
  }
}
