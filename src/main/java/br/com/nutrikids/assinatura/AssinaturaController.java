package br.com.nutrikids.assinatura;

import br.com.nutrikids.auditoria.*;
import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assinatura")
@Transactional
public class AssinaturaController {
  private final AssinaturaRepository repo;
  private final FaturaRepository faturas;

  public AssinaturaController(AssinaturaRepository r, FaturaRepository f) {
    repo = r;
    faturas = f;
  }

  @GetMapping
  public Object obter() {
    AcessoPaciente.admin();
    return repo.findById(Identidade.atual().clinicaId())
        .map(a -> (Object) a)
        .orElse(
            Map.of(
                "status",
                "NAO_CONFIGURADA",
                "ambiente",
                "SANDBOX",
                "aviso",
                "Onboarding e criação de recorrência pendentes de homologação"));
  }

  @GetMapping("/faturas")
  public List<Fatura> faturas() {
    AcessoPaciente.admin();
    return faturas.findByClinicaIdOrderByVencimentoDesc(Identidade.atual().clinicaId());
  }
}
