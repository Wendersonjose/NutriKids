package br.com.nutrikids.paciente;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
public class Paciente {
  @Id public UUID id;

  @Column(nullable = false)
  public UUID clinicaId;

  @Column(nullable = false)
  public String nome;

  @Column(nullable = false)
  public LocalDate nascimento;

  @Column(nullable = false)
  public String sexo;

  @Column(length = 4000)
  public String observacoes;

  public Instant criadoEm;
  @Version public long versao;
}
