package br.com.nutrikids.antropometria;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedidaRepository extends JpaRepository<Medida, UUID> {
  List<Medida> findByPacienteIdAndClinicaIdOrderByDataAsc(UUID p, UUID c);
}
