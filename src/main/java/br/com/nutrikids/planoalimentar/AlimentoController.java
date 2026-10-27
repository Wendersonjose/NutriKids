package br.com.nutrikids.planoalimentar;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/alimentos")
@Transactional
public class AlimentoController {
  private final AlimentoRepository repo;
  private final Auditoria audit;

  public AlimentoController(AlimentoRepository r, Auditoria a) {
    repo = r;
    audit = a;
  }

  public record Dados(
      @NotBlank @Size(max = 255) String nome,
      @NotBlank @Size(max = 255) String fonte,
      @NotBlank @Size(max = 255) String versaoFonte,
      @NotBlank @Size(max = 255) String permissaoUso,
      @NotNull @Size(min = 1, max = 100)
          Map<
                  @Pattern(regexp = "[a-z_]{1,40}") String,
                  @NotNull @DecimalMin("0") @DecimalMax("100000000") BigDecimal>
              nutrientes,
      @NotNull @Size(max = 30)
          Map<
                  @Size(min = 1, max = 60) String,
                  @NotNull @DecimalMin("0.001") @DecimalMax("100000") BigDecimal>
              medidas) {}

  public record Item(
      @NotNull UUID alimentoId,
      @NotNull @DecimalMin("0.001") @DecimalMax("100000") BigDecimal quantidade,
      @Size(max = 60) String medida) {}

  public record Refeicao(@NotEmpty @Size(max = 100) List<@Valid Item> itens) {}

  @GetMapping
  public List<Alimento> listar() {
    AcessoPaciente.profissional();
    return repo.findByClinicaIdOrderByNome(Identidade.atual().clinicaId());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Alimento criar(@Valid @RequestBody Dados d) {
    AcessoPaciente.profissional();
    var a = new Alimento();
    a.id = UUID.randomUUID();
    a.clinicaId = Identidade.atual().clinicaId();
    a.nome = d.nome();
    a.fonte = d.fonte();
    a.versaoFonte = d.versaoFonte();
    a.permissaoUso = d.permissaoUso();
    a.nutrientes = d.nutrientes();
    a.medidas = d.medidas();
    repo.save(a);
    audit.registrar("ALIMENTO_CRIADO", a.id);
    return a;
  }

  @PostMapping("/calcular")
  public Map<String, Object> calcular(@Valid @RequestBody Refeicao d) {
    AcessoPaciente.profissional();
    var itens = new ArrayList<Map<String, BigDecimal>>();
    var fontes = new ArrayList<String>();
    var porcoes = new ArrayList<String>();
    for (var item : d.itens()) {
      var a =
          repo.findByIdAndClinicaId(item.alimentoId(), Identidade.atual().clinicaId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
      BigDecimal gramas = item.quantidade();
      if (item.medida() != null && !item.medida().isBlank()) {
        var fator = a.medidas.get(item.medida());
        if (fator == null)
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Medida caseira não cadastrada");
        gramas = gramas.multiply(fator);
      }
      porcoes.add(a.nome + ": " + gramas + " g");
      itens.add(Composicao.porcao(a.nutrientes, gramas));
      fontes.add(a.fonte + " / " + a.versaoFonte);
    }
    return Map.of(
        "porcoes",
        porcoes,
        "itens",
        itens,
        "totais",
        Composicao.somar(itens),
        "fontes",
        fontes,
        "aviso",
        "Somente nutrientes presentes em TODOS os itens são totalizados. Unidades: energia_kcal;"
            + " macronutrientes_g; demais nutrientes devem informar unidade na chave. Sem"
            + " estimativa de necessidades.");
  }
}
