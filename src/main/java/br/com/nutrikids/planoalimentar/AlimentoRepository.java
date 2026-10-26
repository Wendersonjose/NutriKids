package br.com.nutrikids.planoalimentar;
import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;
public interface AlimentoRepository extends JpaRepository<Alimento,UUID> {
 List<Alimento> findByClinicaIdOrderByNome(UUID clinicaId);
 Optional<Alimento> findByIdAndClinicaId(UUID id,UUID clinicaId);
}
