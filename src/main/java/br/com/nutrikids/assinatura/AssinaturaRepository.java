package br.com.nutrikids.assinatura;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface AssinaturaRepository extends JpaRepository<Assinatura, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Assinatura a where a.provedorId=:id")
  Optional<Assinatura> bloquear(String id);
}
