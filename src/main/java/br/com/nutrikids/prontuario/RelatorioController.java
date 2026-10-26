package br.com.nutrikids.prontuario;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import br.com.nutrikids.prontuario.RelatorioService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.*;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "prontuario")
public class RelatorioController {
  private final RelatorioService service;

  public RelatorioController(RelatorioService service) {
    this.service = service;
  }

  @Operation(
      summary = "Exportar relatório PDF",
      description =
          "Paciente autorizado. Família recebe somente registros compartilhados. Arquivo de"
              + " acompanhamento, não laudo automático. Exportação auditada.")
  @GetMapping(value = "/api/pacientes/{id}/relatorio.pdf", produces = "application/pdf")
  public ResponseEntity<byte[]> pdf(@PathVariable UUID id) throws IOException {
    return service.pdf(id);
  }
}
