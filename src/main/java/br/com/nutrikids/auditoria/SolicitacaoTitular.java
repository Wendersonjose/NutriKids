package br.com.nutrikids.auditoria;
import jakarta.persistence.*;import java.util.*;import java.time.*;
@Entity public class SolicitacaoTitular {
 @Id public UUID id;@Column(nullable=false) public UUID clinicaId;@Column(nullable=false) public UUID pacienteId;@Column(nullable=false) public UUID solicitanteId;
 @Column(nullable=false) public String tipo;@Column(nullable=false) public String status;@Column(nullable=false) public Instant criadaEm;
 @Column(length=2000) public String resposta;public Instant respondidaEm;
 @Version public long versao;
}
