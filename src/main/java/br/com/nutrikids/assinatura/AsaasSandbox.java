package br.com.nutrikids.assinatura;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
/** Destino fixo de sandbox; este adaptador não pode efetuar cobranças em produção. */
@Component public class AsaasSandbox {
 private final RestClient client;private final String token;
 public AsaasSandbox(@Value("${ASAAS_SANDBOX_TOKEN:}") String token){this.token=token;var factory=new org.springframework.http.client.SimpleClientHttpRequestFactory();factory.setConnectTimeout(5000);factory.setReadTimeout(10000);this.client=RestClient.builder().requestFactory(factory).baseUrl("https://api-sandbox.asaas.com/v3").defaultHeader("access_token",token).build();}
 public JsonNode assinatura(String id){return get("subscriptions",id);}
 public JsonNode pagamento(String id){return get("payments",id);}
 private JsonNode get(String tipo,String id){if(token.isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Sandbox não configurado");if(id==null||!id.matches("[A-Za-z0-9_]{1,100}"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
 try{return client.get().uri("/"+tipo+"/"+id).retrieve().body(JsonNode.class);}catch(org.springframework.web.client.RestClientException ex){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Falha ao consultar sandbox");}}
}
