package br.com.nutrikids.paciente;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository
    extends org.springframework.data.jpa.repository.JpaSpecificationExecutor<Paciente>,
        JpaRepository<Paciente, UUID> {
  List<Paciente> findByClinicaIdOrderByNome(UUID id);

  Optional<Paciente> findByIdAndClinicaId(UUID id, UUID clinicaId);
}
