package br.com.nutrikids.usuario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Usuario {
  @Id public UUID id;

  @Column(nullable = false)
  public UUID clinicaId;

  @Column(nullable = false, unique = true)
  public String email;

  @JsonIgnore
  @Column(nullable = false)
  public String senha;

  @Column(nullable = false)
  public String nome;

  @Column(nullable = false)
  public String perfil;

  public String crn;
}
