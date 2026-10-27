package br.com.nutrikids;

import static org.junit.jupiter.api.Assertions.*;

import br.com.nutrikids.planoalimentar.*;
import java.math.*;
import java.util.*;
import org.junit.jupiter.api.*;

class ComposicaoTest {
  @Test
  void porcaoDecimal() {
    var r =
        Composicao.porcao(Map.of("energia_kcal", new BigDecimal("120.25")), new BigDecimal("37.5"));
    assertEquals(0, new BigDecimal("45.09375").compareTo(r.get("energia_kcal")));
  }

  @Test
  void naoConfundeAusenciaComZero() {
    assertFalse(
        Composicao.somar(
                List.of(Map.of("ferro_mg", BigDecimal.ONE), Map.of("energia_kcal", BigDecimal.TEN)))
            .containsKey("ferro_mg"));
  }

  @Test
  void totaliza() {
    assertEquals(
        0,
        BigDecimal.valueOf(3)
            .compareTo(
                Composicao.somar(
                        List.of(
                            Map.of("proteina_g", BigDecimal.ONE),
                            Map.of("proteina_g", BigDecimal.valueOf(2))))
                    .get("proteina_g")));
  }

  @Test
  void rejeitaNegativo() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Composicao.porcao(Map.of("x", BigDecimal.ONE), BigDecimal.valueOf(-1)));
  }

  @Test
  void rendimentoMedidoDaPreparacao() {
    assertEquals(
        0,
        BigDecimal.valueOf(50)
            .compareTo(
                Composicao.preparacaoPor100g(
                        List.of(Map.of("energia_kcal", BigDecimal.valueOf(200))),
                        BigDecimal.valueOf(400))
                    .get("energia_kcal")));
  }
}
