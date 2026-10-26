package br.com.nutrikids.auth;

import br.com.nutrikids.usuario.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Tag(name = "auth")
@RequestMapping("/api/auth")
public class AuthController {
  private final UsuarioRepository users;
  private final PasswordEncoder encoder;
  private final ProtecaoLogin limite;
  private final String dummy;

  public AuthController(UsuarioRepository u, PasswordEncoder e, ProtecaoLogin l) {
    users = u;
    encoder = e;
    limite = l;
    dummy = e.encode(UUID.randomUUID().toString());
  }

  @io.swagger.v3.oas.annotations.media.Schema(
      name = "AuthLogin",
      description = "Contrato Auth Login. Exemplos e identificadores exclusivamente sintéticos.")
  record Login(
      @NotBlank @Email String email,
      @NotBlank
          @Size(max = 72)
          @io.swagger.v3.oas.annotations.media.Schema(
              accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY,
              example = "SenhaSintetica123!")
          String senha) {}

  @Operation(
      summary = "Obter token CSRF",
      description =
          "Público. Cria cookie XSRF-TOKEN; enviar token no header X-XSRF-TOKEN nas mutações.")
  @GetMapping("/csrf")
  Map<String, String> csrf(@io.swagger.v3.oas.annotations.Parameter(hidden = true) CsrfToken t) {
    return Map.of("token", t.getToken());
  }

  @Operation(
      summary = "Autenticar por sessão",
      description =
          "Público com CSRF. Credenciais sintéticas; renova sessão para impedir fixação. Limite"
              + " local 15 tentativas em 15 minutos.")
  @PostMapping("/login")
  Identidade login(@Valid @RequestBody Login r, HttpServletRequest req, HttpServletResponse res) {
    limite.conferir(req.getRemoteAddr());
    if (r.senha().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    var optional = users.findByEmail(r.email().toLowerCase(Locale.ROOT));
    boolean valida = encoder.matches(r.senha(), optional.map(x -> x.senha).orElse(dummy));
    if (!valida || optional.isEmpty())
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    var u = optional.get();
    if (req.getSession(false) != null) {
      req.getSession().invalidate();
    }
    req.getSession(true);
    var i = new Identidade(u.id, u.clinicaId, u.perfil, u.nome);
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
        new UsernamePasswordAuthenticationToken(
            i, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.perfil))));
    SecurityContextHolder.setContext(context);
    new HttpSessionSecurityContextRepository().saveContext(context, req, res);
    return i;
  }

  @Operation(
      summary = "Consultar identidade autenticada",
      description = "Retorna somente identidade da sessão atual.")
  @GetMapping("/me")
  Identidade me() {
    return Identidade.atual();
  }
}
