package br.com.nutrikids.prontuario;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnexoRepository
    extends org.springframework.data.jpa.repository.JpaSpecificationExecutor<Anexo>,
        JpaRepository<Anexo, UUID> {
  List<Anexo> findByPacienteIdAndClinicaIdOrderByCriadoEmDesc(UUID p, UUID c);

  Optional<Anexo> findByIdAndPacienteIdAndClinicaId(UUID id, UUID p, UUID c);
}
