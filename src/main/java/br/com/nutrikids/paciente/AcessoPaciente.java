package br.com.nutrikids.paciente;

import br.com.nutrikids.auth.*;
import br.com.nutrikids.responsavel.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcessoPaciente {
  private final PacienteRepository pacientes;
  private final VinculoRepository vinculos;

  public AcessoPaciente(PacienteRepository p, VinculoRepository v) {
    pacientes = p;
    vinculos = v;
  }

  public Paciente obter(UUID id) {
    var i = Identidade.atual();
    var p =
        pacientes
            .findByIdAndClinicaId(id, i.clinicaId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (i.familia()
        && !vinculos.existsByPacienteIdAndUsuarioIdAndClinicaId(id, i.id(), i.clinicaId()))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    return p;
  }

  public static void profissional() {
    if (Identidade.atual().familia()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  public static void admin() {
    if (!Identidade.atual().perfil().equals("ADMIN"))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }
}
