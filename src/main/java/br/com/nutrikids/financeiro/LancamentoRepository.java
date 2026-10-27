package br.com.nutrikids.financeiro;

import jakarta.persistence.LockModeType;
import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface LancamentoRepository extends JpaRepository<Lancamento, UUID> {
  List<Lancamento> findByClinicaIdAndCompetenciaBetweenOrderByVencimento(
      UUID c, LocalDate inicio, LocalDate fim);

  List<Lancamento> findByClinicaIdAndPagamentoBetweenOrderByPagamento(
      UUID c, LocalDate inicio, LocalDate fim);

  List<Lancamento> findByClinicaIdAndVencimentoBetweenOrderByVencimento(
      UUID c, LocalDate inicio, LocalDate fim);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select l from Lancamento l where l.id=:id and l.clinicaId=:clinica")
  Optional<Lancamento> bloquear(UUID id, UUID clinica);
}
