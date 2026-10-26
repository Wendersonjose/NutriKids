package br.com.nutrikids.prontuario;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import br.com.nutrikids.prontuario.AnexoService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.*;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "prontuario")
@RequestMapping("/api/pacientes/{pacienteId}/anexos")
public class AnexoController {
  private final AnexoService service;

  public AnexoController(AnexoService service) {
    this.service = service;
  }

  @Operation(
      summary = "Listar metadados de anexos",
      description =
          "Paciente autorizado; responsável vê somente compartilhados; conteúdo binário não é"
              + " serializado.")
  @GetMapping
  public List<Vista> listar(
      @PathVariable UUID pacienteId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    return service.listar(pacienteId, page, size);
  }

  @Operation(
      summary = "Enviar anexo protegido",
      description =
          "multipart/form-data; título até 150 caracteres, limite total 5 MB; assinatura"
              + " PDF/PNG/JPEG. Família só envia para paciente vinculado e força compartilhamento."
              + " Não substitui antimalware.")
  @PostMapping(consumes = "multipart/form-data")
  @ResponseStatus(HttpStatus.CREATED)
  public Vista enviar(
      @PathVariable UUID pacienteId,
      @RequestParam MultipartFile arquivo,
      @RequestParam(defaultValue = "false") boolean compartilhado,
      @RequestParam String titulo)
      throws IOException {
    return service.enviar(pacienteId, arquivo, compartilhado, titulo);
  }

  @Operation(
      summary = "Baixar anexo",
      description =
          "Escopo clínica, paciente e compartilhamento. Resposta binária attachment com nosniff e"
              + " sem cache.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "Arquivo autorizado",
      content =
          @io.swagger.v3.oas.annotations.media.Content(
              mediaType = "application/octet-stream",
              schema =
                  @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary")))
  @GetMapping("/{id}")
  public ResponseEntity<byte[]> baixar(@PathVariable UUID pacienteId, @PathVariable UUID id) {
    return service.baixar(pacienteId, id);
  }
}
