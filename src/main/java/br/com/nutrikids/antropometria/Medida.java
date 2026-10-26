package br.com.nutrikids.antropometria;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Entity
public class Medida {
  @Id public UUID id;
  public UUID clinicaId;
  public UUID pacienteId;
  public UUID autorId;
  public LocalDate data;

  @Column(precision = 6, scale = 2)
  public BigDecimal pesoKg;

  @Column(precision = 6, scale = 2)
  public BigDecimal alturaCm;

  public String posicao;
}
