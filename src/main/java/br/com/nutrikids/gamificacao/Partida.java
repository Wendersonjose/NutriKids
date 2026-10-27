package br.com.nutrikids.gamificacao;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
public class Partida {
  @Id public UUID id;

  @Column(nullable = false)
  public UUID clinicaId;

  @Column(nullable = false)
  public UUID pacienteId;

  @Column(nullable = false)
  public String tabuleiro;

  @Column(nullable = false)
  public String encontrados = "";

  public int tentativas;
  public Instant concluida;

  @Column(nullable = false)
  public Instant criadaEm;

  @Version public long versao;
}
