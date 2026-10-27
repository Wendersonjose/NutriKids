package br.com.nutrikids.auditoria;
import br.com.nutrikids.auth.*;import br.com.nutrikids.paciente.*;
import jakarta.validation.*;import jakarta.validation.constraints.*;import java.util.*;import java.time.*;
import org.springframework.web.bind.annotation.*;import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/pacientes/{paciente}/privacidade/solicitacoes") @Transactional
public class PrivacidadeController {
 private final AcessoPaciente acesso;private final SolicitacaoRepository repo;private final Auditoria audit;
 public PrivacidadeController(AcessoPaciente a,SolicitacaoRepository r,Auditoria au){acesso=a;repo=r;audit=au;}
 record Pedido(@NotNull @Pattern(regexp="ACESSO|CORRECAO|EXCLUSAO|INFORMACAO") String tipo){}
 record Resposta(@NotNull @Pattern(regexp="CONCLUIDA|INDEFERIDA") String status,@NotBlank @Size(max=2000) String resposta){}
 @GetMapping public List<SolicitacaoTitular> listar(@PathVariable UUID paciente){acesso.obter(paciente);var i=Identidade.atual();audit.registrar("SOLICITACOES_LIDAS",paciente);return repo.findByPacienteIdAndClinicaIdOrderByCriadaEmDesc(paciente,i.clinicaId()).stream().filter(s->!i.familia()||s.solicitanteId.equals(i.id())).toList();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public SolicitacaoTitular pedir(@PathVariable UUID paciente,@Valid @RequestBody Pedido d){acesso.obter(paciente);var i=Identidade.atual();var s=new SolicitacaoTitular();s.id=UUID.randomUUID();s.clinicaId=i.clinicaId();s.pacienteId=paciente;s.solicitanteId=i.id();s.tipo=d.tipo();s.status="ABERTA";s.criadaEm=Instant.now();repo.save(s);audit.registrar("SOLICITACAO_TITULAR_CRIADA",s.id);return s;}
 @PostMapping("/{id}/resposta") public SolicitacaoTitular responder(@PathVariable UUID paciente,@PathVariable UUID id,@Valid @RequestBody Resposta d){AcessoPaciente.admin();acesso.obter(paciente);var s=repo.bloquear(id,paciente,Identidade.atual().clinicaId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(!s.status.equals("ABERTA"))throw new ResponseStatusException(HttpStatus.CONFLICT);s.status=d.status();s.resposta=d.resposta();s.respondidaEm=Instant.now();audit.registrar("SOLICITACAO_TITULAR_RESPONDIDA",s.id);return s;}
}
