package br.com.nutrikids.usuario;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.clinica.*;
import br.com.nutrikids.paciente.*;
import br.com.nutrikids.responsavel.*;
import br.com.nutrikids.usuario.EquipeService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "usuario")
@RequestMapping("/api")
public class EquipeController {
  private final EquipeService service;

  public EquipeController(EquipeService service) {
    this.service = service;
  }

  @Operation(
      summary = "Listar equipe",
      description = "ADMIN ou NUTRICIONISTA. Nunca retorna hash de senha.")
  @GetMapping("/equipe")
  public List<VistaUsuario> listar(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
    return service.listar(page, size);
  }

  @Operation(
      summary = "Criar usuário",
      description =
          "Apenas ADMIN. Senha de 12 caracteres no mínimo e até 72 bytes UTF-8; perfis ADMIN,"
              + " NUTRICIONISTA, RESPONSAVEL.")
  @PostMapping("/equipe")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public VistaUsuario criar(@Valid @RequestBody Dados d) {
    return service.criar(d);
  }

  @Operation(
      summary = "Vincular responsável",
      description =
          "Apenas ADMIN; usuário RESPONSAVEL e paciente devem pertencer à clínica da sessão.")
  @PostMapping("/pacientes/{id}/responsaveis")
  public void vincular(@PathVariable UUID id, @Valid @RequestBody Vincular d) {
    service.vincular(id, d);
  }

  @Operation(
      summary = "Consultar clínica",
      description = "Equipe clínica; clínica derivada da sessão.")
  @GetMapping("/clinica")
  public VistaClinica clinica() {
    return service.clinica();
  }

  @Operation(summary = "Renomear clínica", description = "Apenas ADMIN.")
  @PutMapping("/clinica")
  public VistaClinica editar(@Valid @RequestBody Nome d) {
    return service.editar(d);
  }
}
