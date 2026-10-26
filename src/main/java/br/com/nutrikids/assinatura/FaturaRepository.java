package br.com.nutrikids.assinatura;
import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;
public interface FaturaRepository extends JpaRepository<Fatura,String> {List<Fatura> findByClinicaIdOrderByVencimentoDesc(UUID clinica);}
