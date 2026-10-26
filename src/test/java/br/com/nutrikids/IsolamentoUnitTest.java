package br.com.nutrikids;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.nutrikids.auth.*;
import br.com.nutrikids.paciente.*;
import br.com.nutrikids.responsavel.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

class IsolamentoUnitTest {
  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void consultaRepositorioSempreComClinicaDaSessao() {
    var repo = mock(PacienteRepository.class);
    var links = mock(VinculoRepository.class);
    var i = new Identidade(UUID.randomUUID(), UUID.randomUUID(), "ADMIN", "Teste");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(i, null, List.of()));
    UUID p = UUID.randomUUID();
    when(repo.findByIdAndClinicaId(p, i.clinicaId())).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> new AcessoPaciente(repo, links).obter(p));
    verify(repo).findByIdAndClinicaId(p, i.clinicaId());
    verifyNoInteractions(links);
  }
}
