package br.com.nutrikids.prontuario;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import java.io.*;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class RelatorioController {
  private final AcessoPaciente acesso;
  private final RegistroRepository repo;
  private final Auditoria audit;

  public RelatorioController(AcessoPaciente a, RegistroRepository r, Auditoria au) {
    acesso = a;
    repo = r;
    audit = au;
  }

  @GetMapping("/api/pacientes/{id}/relatorio.pdf")
  ResponseEntity<byte[]> pdf(@PathVariable UUID id) throws IOException {
    var p = acesso.obter(id);
    var i = Identidade.atual();
    var lines = new ArrayList<String>();
    lines.add("Nutri Kids - Relatorio de acompanhamento");
    lines.add(p.nome + " | Nascimento: " + p.nascimento);
    for (var r : repo.findByPacienteIdAndClinicaIdOrderByCriadoEmDesc(id, i.clinicaId())) {
      if (i.familia() && !r.compartilhado) continue;
      lines.add(r.tipo + " | " + r.criadoEm);
      for (String line : r.conteudo.split("\n")) {
        for (int start = 0; start < line.length(); start += 90)
          lines.add(line.substring(start, Math.min(start + 90, line.length())));
      }
    }
    try (var doc = new PDDocument();
        var out = new ByteArrayOutputStream()) {
      PDPageContentStream stream = null;
      int n = 0;
      for (String l : lines) {
        if (n % 46 == 0) {
          if (stream != null) stream.close();
          var page = new PDPage();
          doc.addPage(page);
          stream = new PDPageContentStream(doc, page);
          stream.beginText();
          stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
          stream.newLineAtOffset(40, 780);
        }
        stream.showText(l.replaceAll("[^\u0020-\u007e\u00a0-\u00ff]", "?"));
        stream.newLineAtOffset(0, -16);
        n++;
        if (n % 46 == 0) stream.endText();
      }
      if (stream != null) {
        if (n % 46 != 0) stream.endText();
        stream.close();
      }
      doc.save(out);
      audit.registrar("RELATORIO_EXPORTADO", id);
      return ResponseEntity.ok()
          .header("Cache-Control", "no-store")
          .header("Content-Disposition", "attachment; filename=acompanhamento.pdf")
          .contentType(MediaType.APPLICATION_PDF)
          .body(out.toByteArray());
    }
  }
}
