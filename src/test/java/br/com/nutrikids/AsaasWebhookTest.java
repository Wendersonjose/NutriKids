package br.com.nutrikids;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.nutrikids.assinatura.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;

class AsaasWebhookTest {
  final String token = "segredo-sintetico-exclusivo-teste-32-caracteres";
  final AsaasSandbox gateway = mock(AsaasSandbox.class);
  final AssinaturaRepository assinaturas = mock(AssinaturaRepository.class);
  final FaturaRepository faturas = mock(FaturaRepository.class);
  final WebhookRepository eventos = mock(WebhookRepository.class);
  final ObjectMapper json = new ObjectMapper();
  final AsaasWebhookController controller =
      new AsaasWebhookController(token, gateway, assinaturas, faturas, eventos);
  final String body =
      "{\"id\":\"evt_1\",\"event\":\"PAYMENT_RECEIVED\",\"payment\":{\"id\":\"pay_test\",\"value\":99999}}";

  @BeforeEach
  void preparar() throws Exception {
    var a = new Assinatura();
    a.clinicaId = UUID.randomUUID();
    a.clienteId = "cus_test";
    a.provedorId = "sub_test";
    a.valor = new BigDecimal("49.90");
    when(assinaturas.bloquear("sub_test")).thenReturn(Optional.of(a));
    when(gateway.pagamento("pay_test"))
        .thenReturn(
            json.readTree(
                "{\"id\":\"pay_test\",\"subscription\":\"sub_test\",\"customer\":\"cus_test\",\"value\":49.90,\"dueDate\":\"2026-10-01\",\"status\":\"RECEIVED\"}"));
  }

  @Test
  void rejeitaTokenAntesDeConsultarGateway() throws Exception {
    assertThrows(
        ResponseStatusException.class, () -> controller.receber("errado", json.readTree(body)));
    verifyNoInteractions(gateway, faturas, eventos);
  }

  @Test
  void persisteValorCanonicoENaoValorDoPayload() throws Exception {
    controller.receber(token, json.readTree(body));
    verify(faturas)
        .save(
            argThat(
                f ->
                    f.valor.compareTo(new BigDecimal("49.90")) == 0
                        && f.status.equals("RECEIVED")));
    verify(eventos).save(argThat(e -> e.id.equals("evt_1")));
  }

  @Test
  void repeticaoNaoReplicaFatura() throws Exception {
    when(eventos.existsById("evt_1")).thenReturn(true);
    controller.receber(token, json.readTree(body));
    verifyNoInteractions(faturas);
    verify(eventos, never()).save(any());
  }

  @Test
  void rejeitaClienteDivergente() throws Exception {
    when(gateway.pagamento("pay_test"))
        .thenReturn(
            json.readTree(
                "{\"id\":\"pay_test\",\"subscription\":\"sub_test\",\"customer\":\"outro\",\"value\":49.90}"));
    assertThrows(
        ResponseStatusException.class, () -> controller.receber(token, json.readTree(body)));
    verifyNoInteractions(faturas);
  }

  @Test
  void sandboxSemCredenciaisFalhaFechado() {
    assertEquals(
        503,
        assertThrows(
                ResponseStatusException.class, () -> new AsaasSandbox("").pagamento("pay_test"))
            .getStatusCode()
            .value());
  }
}
