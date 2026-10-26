package br.com.nutrikids.gamificacao;
import org.springframework.data.jpa.repository.*;import java.util.*;import jakarta.persistence.LockModeType;
public interface PartidaRepository extends JpaRepository<Partida,UUID> {
 List<Partida> findByPacienteIdAndClinicaIdOrderByCriadaEmDesc(UUID paciente,UUID clinica);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Partida p where p.id=:id and p.pacienteId=:paciente and p.clinicaId=:clinica")
 Optional<Partida> bloquear(UUID id,UUID paciente,UUID clinica);
}
