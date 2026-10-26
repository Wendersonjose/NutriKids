package br.com.nutrikids.assinatura;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WebhookRepository extends JpaRepository<WebhookProcessado,String> {}
