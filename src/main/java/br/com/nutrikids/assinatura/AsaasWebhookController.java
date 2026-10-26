package br.com.nutrikids.assinatura;
import com.fasterxml.jackson.databind.JsonNode;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.time.*;
import org.springframework.beans.factory.annotation.Value;import org.springframework.transaction.annotation.Transactional;import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
@RestController public class AsaasWebhookController {
 private final String segredo;private final AsaasSandbox sandbox;private final AssinaturaRepository assinaturas;private final FaturaRepository faturas;private final WebhookRepository eventos;
 public AsaasWebhookController(@Value("${ASAAS_WEBHOOK_TOKEN:}") String s,AsaasSandbox a,AssinaturaRepository ar,FaturaRepository f,WebhookRepository e){segredo=s;sandbox=a;assinaturas=ar;faturas=f;eventos=e;}
 @PostMapping("/api/webhooks/asaas-sandbox") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
 public void receber(@RequestHeader(name="asaas-access-token",defaultValue="") String token,@RequestBody JsonNode body){
 if(segredo.length()<32||!MessageDigest.isEqual(segredo.getBytes(StandardCharsets.UTF_8),token.getBytes(StandardCharsets.UTF_8)))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
 String evento=body.path("id").asText(),tipo=body.path("event").asText();
 if(evento.isBlank()||evento.length()>255)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
 if(!SetHolder.EVENTOS.contains(tipo))return;
 var payment=sandbox.pagamento(body.path("payment").path("id").asText());
 var assinatura=assinaturas.bloquear(payment.path("subscription").asText()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
 // Lock da assinatura serializa eventos concorrentes. Reconsultar a situação canônica evita regressões por eventos fora de ordem.
 if(eventos.existsById(evento))return;
 if(!assinatura.clienteId.equals(payment.path("customer").asText())||!payment.path("value").isNumber()||assinatura.valor.compareTo(payment.path("value").decimalValue())!=0)throw new ResponseStatusException(HttpStatus.CONFLICT);
 var f=new Fatura();f.id=payment.path("id").asText();f.clinicaId=assinatura.clinicaId;f.valor=payment.path("value").decimalValue();f.status=payment.path("status").asText();
 try{f.vencimento=LocalDate.parse(payment.path("dueDate").asText());}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);}
 faturas.save(f);var e=new WebhookProcessado();e.id=evento;e.recebidoEm=Instant.now();eventos.save(e);
 // Nenhuma liberação/bloqueio clínico automático nesta etapa: política comercial e carência ainda não homologadas.
 }
 private static class SetHolder {static final java.util.Set<String> EVENTOS=java.util.Set.of("PAYMENT_CREATED","PAYMENT_UPDATED","PAYMENT_CONFIRMED","PAYMENT_RECEIVED","PAYMENT_OVERDUE","PAYMENT_DELETED","PAYMENT_REFUNDED");}
}
