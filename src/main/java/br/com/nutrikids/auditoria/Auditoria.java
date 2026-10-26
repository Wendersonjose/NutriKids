package br.com.nutrikids.auditoria;

import br.com.nutrikids.auth.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class Auditoria {
  private final EventoRepository repo;

  public Auditoria(EventoRepository r) {
    repo = r;
  }

  public void registrar(String acao, UUID id) {
    var i = Identidade.atual();
    var e = new Evento();
    e.id = UUID.randomUUID();
    e.clinicaId = i.clinicaId();
    e.usuarioId = i.id();
    e.recursoId = id;
    e.acao = acao;
    e.instante = Instant.now();
    repo.save(e);
  }
}
