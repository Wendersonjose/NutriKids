package br.com.nutrikids.agenda;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository
    extends org.springframework.data.jpa.repository.JpaSpecificationExecutor<Consulta>,
        JpaRepository<Consulta, UUID> {
  List<Consulta> findByClinicaIdOrderByInicioAsc(UUID c);

  Optional<Consulta> findByIdAndClinicaId(UUID id, UUID c);
}
