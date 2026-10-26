package br.com.nutrikids.paciente;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.PacienteService.*;
import br.com.nutrikids.responsavel.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "paciente")
@RequestMapping("/api/pacientes")
public class PacienteController {
  private final PacienteService service;

  public PacienteController(PacienteService service) {
    this.service = service;
  }

  @Operation(
      summary = "Listar pacientes autorizados",
      description =
          "Filtro nome sem distinção de maiúsculas. Responsável só vê vínculos e nunca observações"
              + " privadas. Paginação no banco antes de serializar.")
  @GetMapping
  public List<Vista> listar(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size,
      @RequestParam(defaultValue = "") String nome) {
    return service.listar(page, size, nome);
  }

  @Operation(
      summary = "Cadastrar paciente",
      description = "ADMIN ou NUTRICIONISTA da clínica autenticada.")
  @PostMapping
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Vista criar(@Valid @RequestBody Dados d) {
    return service.criar(d);
  }

  @Operation(
      summary = "Consultar paciente",
      description =
          "Clínica da sessão; responsável requer vínculo. 404 oculta recursos fora do escopo.")
  @GetMapping("/{id}")
  public Vista obter(@PathVariable UUID id) {
    return service.obter(id);
  }

  @Operation(
      summary = "Atualizar paciente",
      description = "ADMIN ou NUTRICIONISTA; controle otimista de versão no banco.")
  @PutMapping("/{id}")
  public Vista editar(@PathVariable UUID id, @Valid @RequestBody Dados d) {
    return service.editar(id, d);
  }
}
