package br.com.nutrikids.gamificacao;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface PartidaRepository extends JpaRepository<Partida, UUID> {
  List<Partida> findByPacienteIdAndClinicaIdOrderByCriadaEmDesc(UUID paciente, UUID clinica);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select p from Partida p where p.id=:id and p.pacienteId=:paciente and p.clinicaId=:clinica")
  Optional<Partida> bloquear(UUID id, UUID paciente, UUID clinica);
}
