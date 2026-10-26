package br.com.nutrikids;
import br.com.nutrikids.planoalimentar.*;import java.util.*;import java.math.*;import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;
class ComposicaoTest {
 @Test void porcaoDecimal(){var r=Composicao.porcao(Map.of("energia_kcal",new BigDecimal("120.25")),new BigDecimal("37.5"));assertEquals(0,new BigDecimal("45.09375").compareTo(r.get("energia_kcal")));}
 @Test void naoConfundeAusenciaComZero(){assertFalse(Composicao.somar(List.of(Map.of("ferro_mg",BigDecimal.ONE),Map.of("energia_kcal",BigDecimal.TEN))).containsKey("ferro_mg"));}
 @Test void totaliza(){assertEquals(0,BigDecimal.valueOf(3).compareTo(Composicao.somar(List.of(Map.of("proteina_g",BigDecimal.ONE),Map.of("proteina_g",BigDecimal.valueOf(2)))).get("proteina_g")));}
 @Test void rejeitaNegativo(){assertThrows(IllegalArgumentException.class,()->Composicao.porcao(Map.of("x",BigDecimal.ONE),BigDecimal.valueOf(-1)));}
}
