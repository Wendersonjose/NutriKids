package br.com.nutrikids.auditoria;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
public class Evento {
  @Id public UUID id;
  public UUID clinicaId;
  public UUID usuarioId;
  public UUID recursoId;
  public String acao;
  public Instant instante;
}
