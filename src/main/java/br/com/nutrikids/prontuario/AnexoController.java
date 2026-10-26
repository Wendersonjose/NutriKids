package br.com.nutrikids.prontuario;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import java.io.*;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pacientes/{pacienteId}/anexos")
public class AnexoController {
  private final AnexoRepository repo;
  private final AcessoPaciente acesso;
  private final Auditoria audit;

  public AnexoController(AnexoRepository r, AcessoPaciente a, Auditoria au) {
    repo = r;
    acesso = a;
    audit = au;
  }

  @GetMapping
  List<Anexo> listar(@PathVariable UUID pacienteId) {
    acesso.obter(pacienteId);
    var i = Identidade.atual();
    audit.registrar("ANEXOS_LISTADOS", pacienteId);
    return repo.findByPacienteIdAndClinicaIdOrderByCriadoEmDesc(pacienteId, i.clinicaId()).stream()
        .filter(a -> !i.familia() || a.compartilhado)
        .toList();
  }

  @PostMapping
  @Transactional
  @ResponseStatus(HttpStatus.CREATED)
  Anexo enviar(
      @PathVariable UUID pacienteId,
      @RequestParam MultipartFile arquivo,
      @RequestParam(defaultValue = "false") boolean compartilhado,
      @RequestParam String titulo)
      throws IOException {
    acesso.obter(pacienteId);
    if (titulo.isBlank()
        || titulo.length() > 150
        || arquivo.isEmpty()
        || arquivo.getSize() > 5 * 1024 * 1024)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título ou tamanho inválido");
    var bytes = arquivo.getBytes();
    String mime = tipo(bytes);
    var i = Identidade.atual();
    var a = new Anexo();
    a.id = UUID.randomUUID();
    a.clinicaId = i.clinicaId();
    a.pacienteId = pacienteId;
    a.autorId = i.id();
    a.titulo = titulo;
    a.mime = mime;
    a.compartilhado = i.familia() || compartilhado;
    a.conteudo = bytes;
    a.criadoEm = Instant.now();
    repo.save(a);
    audit.registrar("ANEXO_CRIADO", a.id);
    return a;
  }

  static String tipo(byte[] b) {
    if (b.length >= 5 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-')
      return "application/pdf";
    if (b.length >= 8
        && (b[0] & 255) == 137
        && b[1] == 'P'
        && b[2] == 'N'
        && b[3] == 'G'
        && b[4] == 13
        && b[5] == 10
        && b[6] == 26
        && b[7] == 10) return "image/png";
    if (b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255)
      return "image/jpeg";
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas PDF, PNG ou JPEG");
  }

  @GetMapping("/{id}")
  ResponseEntity<byte[]> baixar(@PathVariable UUID pacienteId, @PathVariable UUID id) {
    acesso.obter(pacienteId);
    var i = Identidade.atual();
    var a =
        repo.findByIdAndPacienteIdAndClinicaId(id, pacienteId, i.clinicaId())
            .filter(x -> !i.familia() || x.compartilhado)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    audit.registrar("ANEXO_BAIXADO", id);
    String ext =
        a.mime.equals("application/pdf") ? "pdf" : a.mime.equals("image/png") ? "png" : "jpg";
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(a.mime))
        .header("Content-Disposition", "attachment; filename=anexo-" + a.id + "." + ext)
        .header("X-Content-Type-Options", "nosniff")
        .body(a.conteudo);
  }
}
