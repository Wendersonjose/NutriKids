package br.com.nutrikids.prontuario;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroRepository
    extends org.springframework.data.jpa.repository.JpaSpecificationExecutor<Registro>,
        JpaRepository<Registro, UUID> {
  List<Registro> findByPacienteIdAndClinicaIdOrderByCriadoEmDesc(UUID p, UUID c);
}
