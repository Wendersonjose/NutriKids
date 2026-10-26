package br.com.nutrikids.clinica;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Clinica {
  @Id public UUID id;

  @Column(nullable = false)
  public String nome;

  public Clinica() {}

  public Clinica(UUID id, String nome) {
    this.id = id;
    this.nome = nome;
  }
}
