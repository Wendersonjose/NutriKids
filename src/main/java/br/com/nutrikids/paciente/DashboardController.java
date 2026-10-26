package br.com.nutrikids.paciente;

import br.com.nutrikids.agenda.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.DashboardService.*;
import br.com.nutrikids.prontuario.*;
import br.com.nutrikids.responsavel.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "paciente")
public class DashboardController {
  private final DashboardService service;

  public DashboardController(DashboardService service) {
    this.service = service;
  }

  @Operation(
      summary = "Consultar indicadores",
      description =
          "Equipe vê sua clínica; família vê vínculos e contagem de registros compartilhados. Sem"
              + " conteúdo clínico nos indicadores.")
  @GetMapping("/api/dashboard")
  public Map<String, Long> dados() {
    return service.dados();
  }
}
