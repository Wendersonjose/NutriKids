package br.com.nutrikids.responsavel;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VinculoRepository extends JpaRepository<Vinculo, UUID> {
  boolean existsByPacienteIdAndUsuarioIdAndClinicaId(UUID p, UUID u, UUID c);

  List<Vinculo> findByUsuarioIdAndClinicaId(UUID u, UUID c);
}
