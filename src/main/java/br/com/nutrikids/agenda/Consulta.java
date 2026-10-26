package br.com.nutrikids.agenda;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
public class Consulta {
  @Id public UUID id;
  public UUID clinicaId;
  public UUID pacienteId;
  public UUID profissionalId;
  public OffsetDateTime inicio;
  public OffsetDateTime fim;
  public String status;
  @Version public long versao;
}
