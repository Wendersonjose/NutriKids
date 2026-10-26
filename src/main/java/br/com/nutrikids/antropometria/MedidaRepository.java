package br.com.nutrikids.antropometria;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedidaRepository
    extends org.springframework.data.jpa.repository.JpaSpecificationExecutor<Medida>,
        JpaRepository<Medida, UUID> {
  List<Medida> findByPacienteIdAndClinicaIdOrderByDataAsc(UUID p, UUID c);
}
