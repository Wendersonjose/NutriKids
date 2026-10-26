package br.com.nutrikids.prontuario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
public class Anexo {
  @Id public UUID id;
  public UUID clinicaId;
  public UUID pacienteId;
  public UUID autorId;
  public String titulo;
  public String mime;
  public boolean compartilhado;
  public Instant criadoEm;

  @JsonIgnore
  @Column(columnDefinition = "bytea")
  public byte[] conteudo;
}
