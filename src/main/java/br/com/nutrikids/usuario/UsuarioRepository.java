package br.com.nutrikids.usuario;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
  Optional<Usuario> findByEmail(String email);

  java.util.List<Usuario> findByClinicaId(UUID id);
}
