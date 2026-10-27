package br.com.nutrikids.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Limite por identidade em processo único. Não confia em X-Forwarded-For. */
public class LimiteApi extends OncePerRequestFilter {
  private record Janela(long inicio, int quantidade) {}

  private final Map<String, Janela> janelas = new HashMap<>();

  synchronized boolean permitir(String chave, long agora) {
    janelas.entrySet().removeIf(e -> agora - e.getValue().inicio >= 60000);
    var j = janelas.getOrDefault(chave, new Janela(agora, 0));
    if (j.quantidade >= 240 || janelas.size() >= 10000 && !janelas.containsKey(chave)) return false;
    janelas.put(chave, new Janela(j.inicio, j.quantidade + 1));
    return true;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest r, HttpServletResponse s, FilterChain c)
      throws ServletException, IOException {
    var a = SecurityContextHolder.getContext().getAuthentication();
    String chave =
        a != null && a.getPrincipal() instanceof Identidade i
            ? "usuario:" + i.id()
            : "ip:" + r.getRemoteAddr();
    if (!permitir(chave, System.currentTimeMillis())) {
      s.setHeader("Retry-After", "60");
      s.sendError(429);
      return;
    }
    c.doFilter(r, s);
  }
}
