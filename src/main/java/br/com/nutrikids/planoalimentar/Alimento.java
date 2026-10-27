package br.com.nutrikids.planoalimentar;

import jakarta.persistence.*;
import java.math.*;
import java.util.*;

@Entity
public class Alimento {
  @Id public UUID id;

  @Column(nullable = false)
  public UUID clinicaId;

  @Column(nullable = false)
  public String nome;

  @Column(nullable = false)
  public String fonte;

  @Column(nullable = false)
  public String versaoFonte;

  @Column(nullable = false)
  public String permissaoUso;

  @ElementCollection
  @CollectionTable(name = "alimento_nutriente", joinColumns = @JoinColumn(name = "alimento_id"))
  @MapKeyColumn(name = "nutriente")
  @Column(name = "valor", precision = 18, scale = 6)
  public Map<String, BigDecimal> nutrientes = new HashMap<>();

  @ElementCollection
  @CollectionTable(name = "alimento_medida", joinColumns = @JoinColumn(name = "alimento_id"))
  @MapKeyColumn(name = "medida")
  @Column(name = "gramas", precision = 18, scale = 6)
  public Map<String, BigDecimal> medidas = new HashMap<>();
}
