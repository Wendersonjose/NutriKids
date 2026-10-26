package br.com.nutrikids.prontuario;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class RegistroService {
  @io.swagger.v3.oas.annotations.media.Schema(
      name = "RegistroVista",
      description =
          "Contrato Registro Vista. Exemplos e identificadores exclusivamente sintéticos.")
  public record Vista(
      UUID id,
      UUID pacienteId,
      UUID autorId,
      String tipo,
      String conteudo,
      boolean compartilhado,
      Instant criadoEm) {}

  private Vista vista(Registro c) {
    return new Vista(
        c.id, c.pacienteId, c.autorId, c.tipo, c.conteudo, c.compartilhado, c.criadoEm);
  }

  private final RegistroRepository repo;
  private final AcessoPaciente acesso;
  private final Auditoria audit;

  public RegistroService(RegistroRepository r, AcessoPaciente a, Auditoria au) {
    repo = r;
    acesso = a;
    audit = au;
  }

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "RegistroDados",
      description =
          "Contrato Registro Dados. Exemplos e identificadores exclusivamente sintéticos.")
  public record Dados(
      @NotNull
          @Pattern(
              regexp =
                  "PRONTUARIO|ANAMNESE|PLANO_ALIMENTAR|DIARIO_ALIMENTAR|TERAPIA_ALIMENTAR|MENSAGEM|META")
          String tipo,
      @NotBlank @Size(max = 12000) String conteudo,
      boolean compartilhado) {}

  public List<Vista> listar(UUID pacienteId, int page, int size) {
    acesso.obter(pacienteId);
    audit.registrar("HISTORICO_LIDO", pacienteId);
    var i = Identidade.atual();
    return repo
        .findAll(
            (root, query, cb) -> {
              var i2 = Identidade.atual();
              var filtro =
                  cb.and(
                      cb.equal(root.get("clinicaId"), i2.clinicaId()),
                      cb.equal(root.get("pacienteId"), pacienteId));
              return i2.familia() ? cb.and(filtro, cb.isTrue(root.get("compartilhado"))) : filtro;
            },
            Paginacao.descendente(page, size, "criadoEm", "id"))
        .stream()
        .filter(r -> !i.familia() || r.compartilhado)
        .map(this::vista)
        .toList();
  }

  @Transactional
  public Vista criar(UUID pacienteId, @Valid Dados d) {
    acesso.obter(pacienteId);
    var i = Identidade.atual();
    if (i.familia() && !Set.of("DIARIO_ALIMENTAR", "MENSAGEM").contains(d.tipo()))
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.FORBIDDEN);
    var r = new Registro();
    r.id = UUID.randomUUID();
    r.clinicaId = i.clinicaId();
    r.pacienteId = pacienteId;
    r.autorId = i.id();
    r.tipo = d.tipo();
    r.conteudo = d.conteudo();
    r.compartilhado = i.familia() || d.compartilhado();
    r.criadoEm = Instant.now();
    repo.save(r);
    audit.registrar("REGISTRO_" + d.tipo(), r.id);
    return vista(r);
  }
}
