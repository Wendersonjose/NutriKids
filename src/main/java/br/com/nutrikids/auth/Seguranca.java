package br.com.nutrikids.auth;

import br.com.nutrikids.usuario.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;

@Configuration
public class Seguranca {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  SecurityFilterChain filter(HttpSecurity h) throws Exception {
    return h.csrf(
            c ->
                c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml")
                    .denyAll()
                    .requestMatchers("/api/auth/csrf", "/api/auth/login")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint((r, s, x) -> s.sendError(401))
                    .accessDeniedHandler((r, s, x) -> s.sendError(403)))
        .logout(
            l ->
                l.logoutUrl("/api/auth/logout").logoutSuccessHandler((r, s, a) -> s.setStatus(204)))
        .sessionManagement(s -> s.sessionFixation(f -> f.migrateSession()))
        .headers(
            x ->
                x.contentSecurityPolicy(
                    c -> c.policyDirectives("default-src 'none'; frame-ancestors 'none'")))
        .build();
  }
}
