package br.com.nutrikids.assinatura;
import jakarta.persistence.*;import java.time.*;
@Entity public class WebhookProcessado {@Id public String id;@Column(nullable=false) public Instant recebidoEm;}
