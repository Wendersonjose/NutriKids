package br.com.nutrikids.antropometria;

import br.com.nutrikids.antropometria.MedidaService.*;
import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "antropometria")
@RequestMapping("/api/pacientes/{pacienteId}/medidas")
public class MedidaController {
  private final MedidaService service;

  public MedidaController(MedidaService service) {
    this.service = service;
  }

  @Operation(
      summary = "Listar medidas",
      description =
          "Paciente autorizado. Lista paginada por data e id. Sem interpretação clínica"
              + " automática.")
  @GetMapping
  public List<Vista> listar(
      @PathVariable UUID pacienteId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    return service.listar(pacienteId, page, size);
  }

  @Operation(
      summary = "Registrar medida",
      description =
          "Equipe clínica. Data entre nascimento e hoje, peso 0.1..400 kg, altura 20..250 cm,"
              + " posição DEITADO ou EM_PE.")
  @PostMapping
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Vista criar(@PathVariable UUID pacienteId, @Valid @RequestBody Dados d) {
    return service.criar(pacienteId, d);
  }
}
