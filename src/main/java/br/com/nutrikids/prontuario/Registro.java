package br.com.nutrikids.prontuario;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
public class Registro {
  @Id public UUID id;
  public UUID clinicaId;
  public UUID pacienteId;
  public UUID autorId;
  public String tipo;

  @Column(length = 12000)
  public String conteudo;

  public boolean compartilhado;
  public Instant criadoEm;
}
