package br.com.nutrikids.gamificacao;
import br.com.nutrikids.paciente.*;import br.com.nutrikids.auth.*;import br.com.nutrikids.auditoria.*;
import jakarta.validation.*;import jakarta.validation.constraints.*;import java.util.*;import java.time.*;
import org.springframework.transaction.annotation.Transactional;import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/pacientes/{paciente}/jogos/memoria") @Transactional
public class JogoController {
 private final AcessoPaciente acesso;private final PartidaRepository repo;private final Auditoria audit;
 public JogoController(AcessoPaciente a,PartidaRepository r,Auditoria au){acesso=a;repo=r;audit=au;}
 record Jogada(@Min(0) @Max(11) int primeira,@Min(0) @Max(11) int segunda) {}
 Map<String,Object> vista(Partida p) {
 var cartas=p.tabuleiro.split(",");var visiveis=new ArrayList<String>();
 for(int n=0;n<12;n++)visiveis.add(p.encontrados.contains("|"+n+"|")?cartas[n]:"?");
 return Map.of("id",p.id,"cartas",visiveis,"tentativas",p.tentativas,"concluida",p.concluida!=null,"conquista",p.concluida!=null?"Explorador dos alimentos":"Em descoberta","criadaEm",p.criadaEm);
 }
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> criar(@PathVariable UUID paciente){
 acesso.obter(paciente);var p=new Partida();p.id=UUID.randomUUID();p.clinicaId=Identidade.atual().clinicaId();p.pacienteId=paciente;p.criadaEm=Instant.now();
 var cartas=new ArrayList<>(List.of("banana","cenoura","uva","tomate","milho","abacate","banana","cenoura","uva","tomate","milho","abacate"));Collections.shuffle(cartas,new java.security.SecureRandom());p.tabuleiro=String.join(",",cartas);repo.save(p);audit.registrar("JOGO_INICIADO",p.id);return vista(p);}
 @GetMapping public List<?> listar(@PathVariable UUID paciente){acesso.obter(paciente);return repo.findByPacienteIdAndClinicaIdOrderByCriadaEmDesc(paciente,Identidade.atual().clinicaId()).stream().map(this::vista).toList();}
 @PostMapping("/{id}/jogadas") public Map<String,Object> jogar(@PathVariable UUID paciente,@PathVariable UUID id,@Valid @RequestBody Jogada j){
 acesso.obter(paciente);var p=repo.bloquear(id,paciente,Identidade.atual().clinicaId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
 if(p.concluida!=null||j.primeira()==j.segunda()||p.encontrados.contains("|"+j.primeira()+"|")||p.encontrados.contains("|"+j.segunda()+"|"))throw new ResponseStatusException(HttpStatus.CONFLICT,"Escolha duas cartas disponíveis");
 var c=p.tabuleiro.split(",");p.tentativas++;if(c[j.primeira()].equals(c[j.segunda()]))p.encontrados+="|"+j.primeira()+"||"+j.segunda()+"|";
 if(p.encontrados.chars().filter(ch->ch=='|').count()==24){p.concluida=Instant.now();audit.registrar("JOGO_CONCLUIDO",p.id);}
 var result=new HashMap<>(vista(p));result.put("reveladas",Map.of(String.valueOf(j.primeira()),c[j.primeira()],String.valueOf(j.segunda()),c[j.segunda()]));return result;
 }
}
