package br.com.nutrikids.assinatura;
import jakarta.persistence.*;import java.util.*;import java.math.*;import java.time.*;
@Entity public class Fatura {
 @Id public String id;@Column(nullable=false) public UUID clinicaId;
 @Column(nullable=false,precision=14,scale=2) public BigDecimal valor;@Column(nullable=false) public String status;
 @Column(nullable=false) public LocalDate vencimento;
}
