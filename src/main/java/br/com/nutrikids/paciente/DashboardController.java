package br.com.nutrikids.paciente;

import br.com.nutrikids.agenda.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.prontuario.*;
import br.com.nutrikids.responsavel.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class DashboardController {
  private final PacienteRepository pacientes;
  private final ConsultaRepository consultas;
  private final VinculoRepository vinculos;
  private final RegistroRepository registros;

  public DashboardController(
      PacienteRepository p, ConsultaRepository c, VinculoRepository v, RegistroRepository r) {
    pacientes = p;
    consultas = c;
    vinculos = v;
    registros = r;
  }

  @GetMapping("/api/dashboard")
  Map<String, Long> dados() {
    var i = Identidade.atual();
    if (i.familia()) {
      var ids =
          vinculos.findByUsuarioIdAndClinicaId(i.id(), i.clinicaId()).stream()
              .map(v -> v.pacienteId)
              .distinct()
              .toList();
      long total = 0;
      for (var id : ids)
        total +=
            registros.findByPacienteIdAndClinicaIdOrderByCriadoEmDesc(id, i.clinicaId()).stream()
                .filter(r -> r.compartilhado)
                .count();
      return Map.of(
          "pacientes",
          (long) ids.size(),
          "compartilhados",
          total,
          "consultasHoje",
          0L,
          "agendadas",
          0L);
    }
    var all = consultas.findByClinicaIdOrderByInicioAsc(i.clinicaId());
    var zone = ZoneId.of("America/Sao_Paulo");
    var hoje = LocalDate.now(zone);
    return Map.of(
        "pacientes",
        (long) pacientes.findByClinicaIdOrderByNome(i.clinicaId()).size(),
        "consultasHoje",
        all.stream()
            .filter(
                c ->
                    !c.status.equals("CANCELADA")
                        && c.inicio.atZoneSameInstant(zone).toLocalDate().equals(hoje))
            .count(),
        "agendadas",
        all.stream()
            .filter(c -> c.status.equals("AGENDADA") && c.inicio.toInstant().isAfter(Instant.now()))
            .count(),
        "compartilhados",
        0L);
  }
}
