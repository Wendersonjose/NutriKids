package br.com.nutrikids.responsavel;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Vinculo {
  @Id public UUID id;
  public UUID clinicaId;
  public UUID pacienteId;
  public UUID usuarioId;
  public String parentesco;
}
