package br.com.nutrikids.prontuario;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import br.com.nutrikids.prontuario.RegistroService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "prontuario")
@RequestMapping("/api/pacientes/{pacienteId}/registros")
public class RegistroController {
  private final RegistroService service;

  public RegistroController(RegistroService service) {
    this.service = service;
  }

  @Operation(
      summary = "Listar registros clínicos",
      description =
          "Paciente autorizado; família vê somente compartilhados. Lista paginada por criação e"
              + " id.")
  @GetMapping
  public List<Vista> listar(
      @PathVariable UUID pacienteId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    return service.listar(pacienteId, page, size);
  }

  @Operation(
      summary = "Acrescentar registro",
      description =
          "Equipe pode registrar tipos permitidos. Responsável só cria DIARIO_ALIMENTAR ou MENSAGEM"
              + " e sempre compartilhado. Sem edição/exclusão.")
  @PostMapping
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Vista criar(@PathVariable UUID pacienteId, @Valid @RequestBody Dados d) {
    return service.criar(pacienteId, d);
  }
}
