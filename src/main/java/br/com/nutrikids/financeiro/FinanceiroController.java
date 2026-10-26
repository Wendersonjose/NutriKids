package br.com.nutrikids.financeiro;
import br.com.nutrikids.auth.*;import br.com.nutrikids.paciente.*;import br.com.nutrikids.auditoria.*;
import jakarta.validation.*;import jakarta.validation.constraints.*;import java.math.*;import java.time.*;import java.util.*;
import org.springframework.transaction.annotation.Transactional;import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api/financeiro") @Transactional
public class FinanceiroController {
 private final LancamentoRepository repo;private final Auditoria audit;
 public FinanceiroController(LancamentoRepository r,Auditoria a){repo=r;audit=a;}
 record Dados(@NotNull @Pattern(regexp="RECEITA|DESPESA") String tipo,@NotBlank @Size(max=255) String descricao,@NotBlank @Size(max=255) String categoria,@NotBlank @Size(max=255) String conta,@NotNull @DecimalMin("0.01") @DecimalMax("999999999999.99") @Digits(integer=12,fraction=2) BigDecimal valor,@NotNull LocalDate competencia,@NotNull LocalDate vencimento) {}
 record Baixa(@NotNull @PastOrPresent LocalDate pagamento,@NotBlank @Size(max=255) String referencia) {}
 @GetMapping public List<Lancamento> listar(@RequestParam LocalDate inicio,@RequestParam LocalDate fim) {
 AcessoPaciente.admin();if(fim.isBefore(inicio)||inicio.plusYears(2).isBefore(fim))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
 audit.registrar("FINANCEIRO_LIDO",null);return repo.findByClinicaIdAndCompetenciaBetweenOrderByVencimento(Identidade.atual().clinicaId(),inicio,fim);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Lancamento criar(@Valid @RequestBody Dados d) {
 AcessoPaciente.admin();var l=new Lancamento();l.id=UUID.randomUUID();l.clinicaId=Identidade.atual().clinicaId();l.tipo=d.tipo();l.descricao=d.descricao();l.categoria=d.categoria();l.conta=d.conta();l.valor=d.valor();l.competencia=d.competencia();l.vencimento=d.vencimento();repo.save(l);audit.registrar("LANCAMENTO_CRIADO",l.id);return l;}
 @PostMapping("/{id}/liquidar") public Lancamento liquidar(@PathVariable UUID id,@Valid @RequestBody Baixa d) {
 AcessoPaciente.admin();var l=repo.bloquear(id,Identidade.atual().clinicaId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
 if(l.pagamento!=null) {if(l.pagamento.equals(d.pagamento())&&Objects.equals(l.referenciaConciliacao,d.referencia()))return l;throw new ResponseStatusException(HttpStatus.CONFLICT,"Lançamento já liquidado");}
 l.pagamento=d.pagamento();l.referenciaConciliacao=d.referencia();audit.registrar("LANCAMENTO_LIQUIDADO",l.id);return l;}
 @GetMapping("/resumo") public Map<String,BigDecimal> resumo(@RequestParam LocalDate inicio,@RequestParam LocalDate fim) {
 var linhas=listar(inicio,fim);BigDecimal receita=BigDecimal.ZERO,despesa=BigDecimal.ZERO,receber=BigDecimal.ZERO,pagar=BigDecimal.ZERO;
 for(var l:linhas) {if(l.tipo.equals("RECEITA")){if(l.pagamento!=null)receita=receita.add(l.valor);else receber=receber.add(l.valor);}else {if(l.pagamento!=null)despesa=despesa.add(l.valor);else pagar=pagar.add(l.valor);}}
 return Map.of("receitasLiquidadas",receita,"despesasLiquidadas",despesa,"saldoLiquidado",receita.subtract(despesa),"aReceber",receber,"aPagar",pagar);}

 @GetMapping("/vencimentos") public List<Lancamento> vencimentos(@RequestParam LocalDate inicio,@RequestParam LocalDate fim) {
 AcessoPaciente.admin();validarPeriodo(inicio,fim);audit.registrar("VENCIMENTOS_LIDOS",null);return repo.findByClinicaIdAndVencimentoBetweenOrderByVencimento(Identidade.atual().clinicaId(),inicio,fim);}
 @GetMapping("/fluxo-caixa") public Map<LocalDate,BigDecimal> fluxo(@RequestParam LocalDate inicio,@RequestParam LocalDate fim) {
 AcessoPaciente.admin();validarPeriodo(inicio,fim);audit.registrar("FLUXO_CAIXA_LIDO",null);var dias=new TreeMap<LocalDate,BigDecimal>();
 for(var l:repo.findByClinicaIdAndPagamentoBetweenOrderByPagamento(Identidade.atual().clinicaId(),inicio,fim))dias.merge(l.pagamento,l.tipo.equals("RECEITA")?l.valor:l.valor.negate(),BigDecimal::add);return dias;}
 private void validarPeriodo(LocalDate inicio,LocalDate fim){if(fim.isBefore(inicio)||inicio.plusYears(2).isBefore(fim))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);}
}
