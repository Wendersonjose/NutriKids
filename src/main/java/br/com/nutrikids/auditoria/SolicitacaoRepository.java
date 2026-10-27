package br.com.nutrikids.auditoria;
import org.springframework.data.jpa.repository.*;import java.util.*;import jakarta.persistence.LockModeType;
public interface SolicitacaoRepository extends JpaRepository<SolicitacaoTitular,UUID> {
 List<SolicitacaoTitular> findByPacienteIdAndClinicaIdOrderByCriadaEmDesc(UUID p,UUID c);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from SolicitacaoTitular s where s.id=:id and s.pacienteId=:paciente and s.clinicaId=:clinica") Optional<SolicitacaoTitular> bloquear(UUID id,UUID paciente,UUID clinica);
}
