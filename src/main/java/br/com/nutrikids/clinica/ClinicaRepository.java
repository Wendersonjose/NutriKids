package br.com.nutrikids.clinica;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicaRepository extends JpaRepository<Clinica, UUID> {}
