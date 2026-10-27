package br.com.nutrikids.antropometria;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class OmsServiceTest {
  final OmsService oms = new OmsService();

  @Test
  void referenciaOficialPesoMenina1522Dias() {
    assertEquals(0.24, oms.calcular("wei", "F", 1522, 17, 100, "EM_PE", false).z(), 0.000001);
  }

  @Test
  void referenciaOficialComprimentoMenino44Dias() {
    assertEquals(-3.29, oms.calcular("len", "M", 44, 4, 50, "DEITADO", false).z(), 0.000001);
  }

  @Test
  void medianaNascimentoAmbosSexos() {
    assertEquals(0, oms.calcular("wei", "M", 0, 3.3464, 49.8842, "DEITADO", false).z());
    assertEquals(0, oms.calcular("wei", "F", 0, 3.2322, 49, "DEITADO", false).z());
  }

  @Test
  void extremosEEdema() {
    assertTrue(oms.calcular("wei", "F", 1, 10.37, 49, "DEITADO", false).inconsistente());
    assertNull(oms.calcular("wei", "F", 1, 3, 49, "DEITADO", true).z());
    assertNotNull(oms.calcular("len", "F", 1, 3, 49, "DEITADO", true).z());
  }

  @Test
  void limites() {
    assertNull(oms.calcular("wei", "M", 1827, 12, 80, "EM_PE", false).z());
    assertNotNull(oms.calcular("wei", "M", 1826, 12, 80, "EM_PE", false).z());
    assertNull(oms.calcular("wfl", "M", 100, 3, 44.9, "DEITADO", false).z());
    assertNull(oms.calcular("wei", "NAO_INFORMADO", 100, 3, 50, "DEITADO", false).z());
  }

  @Test
  void ajustePosicao() {
    assertEquals(
        oms.calcular("len", "M", 730, 10, 80.7, "DEITADO", false).z(),
        oms.calcular("len", "M", 730, 10, 80, "EM_PE", false).z());
    assertEquals(
        oms.calcular("len", "M", 731, 10, 80, "EM_PE", false).z(),
        oms.calcular("len", "M", 731, 10, 80.7, "DEITADO", false).z());
  }

  @Test
  void lZeroENormal() {
    var p = new OmsService.Lms(0, 10, 0.1);
    assertEquals(1, OmsService.escore(p, 10 * Math.exp(0.1), true), 1e-10);
    assertEquals(50, OmsService.percentil(0), 0.00001);
    assertEquals(97.7249868, OmsService.percentil(2), 0.00001);
  }

  @Test
  void rejeitaNaN() {
    assertThrows(
        IllegalArgumentException.class,
        () -> oms.calcular("wei", "F", 10, Double.NaN, 50, "DEITADO", false));
  }
}
