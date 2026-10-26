package br.com.nutrikids.agenda;

import br.com.nutrikids.agenda.AgendaService.*;
import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import br.com.nutrikids.usuario.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "agenda")
@RequestMapping("/api/agenda")
public class AgendaController {
  private final AgendaService service;

  public AgendaController(AgendaService service) {
    this.service = service;
  }

  @Operation(
      summary = "Listar agenda",
      description =
          "Equipe clínica. Filtros profissionalId e início inclusivo/fim exclusivo por início da"
              + " consulta. page/size limitam resultados.")
  @GetMapping
  public List<Vista> listar(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size,
      @RequestParam(required = false) UUID profissionalId,
      @RequestParam(required = false) OffsetDateTime inicio,
      @RequestParam(required = false) OffsetDateTime fim) {
    return service.listar(page, size, profissionalId, inicio, fim);
  }

  @Operation(
      summary = "Agendar consulta",
      description =
          "Equipe clínica. Paciente e profissional da mesma clínica. Intervalo positivo até 8 horas"
              + " exatas. Sobreposição de paciente ou profissional retorna 409 no PostgreSQL,"
              + " inclusive concorrente.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Vista criar(@Valid @RequestBody Dados d) {
    return service.criar(d);
  }

  @Operation(
      summary = "Encerrar consulta",
      description =
          "Equipe clínica; transição de AGENDADA para CANCELADA ou CONCLUIDA."
              + " Repetição/concorrência retorna 409.")
  @PatchMapping("/{id}")
  public Vista estado(@PathVariable UUID id, @Valid @RequestBody Estado d) {
    return service.estado(id, d);
  }
}
