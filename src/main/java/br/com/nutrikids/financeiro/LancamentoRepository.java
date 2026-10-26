package br.com.nutrikids.financeiro;
import java.util.*;import java.time.*;import org.springframework.data.jpa.repository.*;import jakarta.persistence.LockModeType;
public interface LancamentoRepository extends JpaRepository<Lancamento,UUID> {
 List<Lancamento> findByClinicaIdAndCompetenciaBetweenOrderByVencimento(UUID c,LocalDate inicio,LocalDate fim);
 List<Lancamento> findByClinicaIdAndPagamentoBetweenOrderByPagamento(UUID c,LocalDate inicio,LocalDate fim);
 List<Lancamento> findByClinicaIdAndVencimentoBetweenOrderByVencimento(UUID c,LocalDate inicio,LocalDate fim);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select l from Lancamento l where l.id=:id and l.clinicaId=:clinica")
 Optional<Lancamento> bloquear(UUID id,UUID clinica);
}
