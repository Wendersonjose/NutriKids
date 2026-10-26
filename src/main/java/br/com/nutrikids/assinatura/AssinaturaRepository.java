package br.com.nutrikids.assinatura;
import java.util.*;import org.springframework.data.jpa.repository.*;import jakarta.persistence.LockModeType;
public interface AssinaturaRepository extends JpaRepository<Assinatura,UUID> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from Assinatura a where a.provedorId=:id") Optional<Assinatura> bloquear(String id);
}
