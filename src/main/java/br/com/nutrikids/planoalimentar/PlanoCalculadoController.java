package br.com.nutrikids.planoalimentar;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import br.com.nutrikids.planoalimentar.AlimentoController.Item;
import br.com.nutrikids.prontuario.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes/{paciente}/plano-calculado")
public class PlanoCalculadoController {
  private final AcessoPaciente acesso;
  private final AlimentoController alimentos;
  private final RegistroRepository registros;
  private final Auditoria audit;

  public PlanoCalculadoController(
      AcessoPaciente a, AlimentoController c, RegistroRepository r, Auditoria au) {
    acesso = a;
    alimentos = c;
    registros = r;
    audit = au;
  }

  public record Refeicao(
      @NotBlank @Size(max = 120) String nome, @NotEmpty @Size(max = 30) List<@Valid Item> itens) {}

  public record Plano(
      @NotBlank @Size(max = 120) String titulo,
      @NotEmpty @Size(max = 12) List<@Valid Refeicao> refeicoes,
      boolean compartilhado) {}

  @PostMapping
  @Transactional
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Registro salvar(@PathVariable UUID paciente, @Valid @RequestBody Plano d) {
    AcessoPaciente.profissional();
    acesso.obter(paciente);
    var linhas = new ArrayList<String>();
    var totais = new ArrayList<Map<String, BigDecimal>>();
    linhas.add(d.titulo());
    linhas.add(
        "Composição de alimentos; revisão profissional obrigatória. Não estima necessidades.");
    for (var refeicao : d.refeicoes()) {
      var calculo = alimentos.calcular(new AlimentoController.Refeicao(refeicao.itens()));
      @SuppressWarnings("unchecked")
      var total = (Map<String, BigDecimal>) calculo.get("totais");
      totais.add(total);
      linhas.add(refeicao.nome());
      linhas.add("Porções: " + calculo.get("porcoes"));
      for (var item : refeicao.itens())
        linhas.add(
            "Alimento "
                + item.alimentoId()
                + ": "
                + item.quantidade()
                + " "
                + (item.medida() == null ? "g" : item.medida()));
      linhas.add("Nutrientes: " + total);
      linhas.add("Fontes: " + calculo.get("fontes"));
    }
    linhas.add(
        "Total diário dos nutrientes completos em todas as refeições: " + Composicao.somar(totais));
    var r = new Registro();
    r.id = UUID.randomUUID();
    r.clinicaId = Identidade.atual().clinicaId();
    r.pacienteId = paciente;
    r.autorId = Identidade.atual().id();
    r.tipo = "PLANO_ALIMENTAR";
    r.conteudo = String.join("\n", linhas);
    if (r.conteudo.length() > 12000)
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "Plano excede limite de conteúdo");
    r.compartilhado = d.compartilhado();
    r.criadoEm = Instant.now();
    registros.save(r);
    audit.registrar("PLANO_CALCULADO_CRIADO", r.id);
    return r;
  }
}
