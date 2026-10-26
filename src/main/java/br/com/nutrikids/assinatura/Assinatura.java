package br.com.nutrikids.assinatura;
import jakarta.persistence.*;import java.util.*;import java.math.*;
@Entity public class Assinatura {
 @Id public UUID clinicaId;@Column(nullable=false,unique=true) public String provedorId;
 @Column(nullable=false) public String clienteId;@Column(nullable=false) public String plano;
 @Column(nullable=false,precision=14,scale=2) public BigDecimal valor;@Column(nullable=false) public String status;
 @Version public long versao;
}
