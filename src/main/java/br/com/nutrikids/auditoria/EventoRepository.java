package br.com.nutrikids.auditoria;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, UUID> {}
