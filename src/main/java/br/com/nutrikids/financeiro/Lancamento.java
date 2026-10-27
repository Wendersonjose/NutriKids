package br.com.nutrikids.financeiro;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Entity
public class Lancamento {
  @Id public UUID id;

  @Column(nullable = false)
  public UUID clinicaId;

  @Column(nullable = false)
  public String tipo;

  @Column(nullable = false)
  public String descricao;

  @Column(nullable = false)
  public String categoria;

  @Column(nullable = false)
  public String conta;

  @Column(nullable = false, precision = 14, scale = 2)
  public BigDecimal valor;

  @Column(nullable = false)
  public LocalDate competencia;

  @Column(nullable = false)
  public LocalDate vencimento;

  public LocalDate pagamento;
  public String referenciaConciliacao;
  @Version public long versao;
}
