package br.com.nutrikids.antropometria;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class MedidaService {
  @io.swagger.v3.oas.annotations.media.Schema(
      name = "MedidaVista",
      description = "Contrato Medida Vista. Exemplos e identificadores exclusivamente sintéticos.")
  public record Vista(
      UUID id,
      UUID pacienteId,
      UUID autorId,
      LocalDate data,
      BigDecimal pesoKg,
      BigDecimal alturaCm,
      String posicao) {}

  private Vista vista(Medida c) {
    return new Vista(c.id, c.pacienteId, c.autorId, c.data, c.pesoKg, c.alturaCm, c.posicao);
  }

  private final MedidaRepository repo;
  private final AcessoPaciente acesso;
  private final Auditoria audit;

  public MedidaService(MedidaRepository r, AcessoPaciente a, Auditoria au) {
    repo = r;
    acesso = a;
    audit = au;
  }

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "MedidaDados",
      description = "Contrato Medida Dados. Exemplos e identificadores exclusivamente sintéticos.")
  public record Dados(
      @NotNull @PastOrPresent LocalDate data,
      @NotNull @DecimalMin("0.1") @DecimalMax("400") BigDecimal pesoKg,
      @NotNull @DecimalMin("20") @DecimalMax("250") BigDecimal alturaCm,
      @NotNull @Pattern(regexp = "DEITADO|EM_PE") String posicao) {}

  public List<Vista> listar(UUID pacienteId, int page, int size) {
    acesso.obter(pacienteId);
    audit.registrar("MEDIDAS_LIDAS", pacienteId);
    return repo
        .findAll(
            (root, query, cb) -> {
              var i2 = Identidade.atual();
              var filtro =
                  cb.and(
                      cb.equal(root.get("clinicaId"), i2.clinicaId()),
                      cb.equal(root.get("pacienteId"), pacienteId));
              return filtro;
            },
            Paginacao.de(page, size, "data", "id"))
        .stream()
        .map(this::vista)
        .toList();
  }

  @Transactional
  public Vista criar(UUID pacienteId, @Valid Dados d) {
    AcessoPaciente.profissional();
    var p = acesso.obter(pacienteId);
    if (d.data().isBefore(p.nascimento))
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "Medida anterior ao nascimento");
    var i = Identidade.atual();
    var m = new Medida();
    m.id = UUID.randomUUID();
    m.clinicaId = i.clinicaId();
    m.pacienteId = pacienteId;
    m.autorId = i.id();
    m.data = d.data();
    m.pesoKg = d.pesoKg();
    m.alturaCm = d.alturaCm();
    m.posicao = d.posicao();
    repo.save(m);
    audit.registrar("MEDIDA_CRIADA", m.id);
    return vista(m);
  }
}
