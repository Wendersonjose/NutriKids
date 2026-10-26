package br.com.nutrikids.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev & !prod")
public class OpenApiConfig {
  @Bean
  @Order(1)
  SecurityFilterChain documentacao(HttpSecurity h) throws Exception {
    // Only static documentation resources. API requests retain the original security chain.
    return h.securityMatcher(
            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml")
        .authorizeHttpRequests(a -> a.anyRequest().permitAll())
        .headers(
            x ->
                x.contentSecurityPolicy(
                    c ->
                        c.policyDirectives(
                            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src"
                                + " 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors"
                                + " 'none'")))
        .build();
  }

  @Bean
  OpenAPI contrato() {
    return new OpenAPI()
        .info(
            new Info()
                .title("NutriKids API")
                .version("0.1.0")
                .description(
                    "Desenvolvimento com dados sintéticos. Autenticação por sessão JSESSIONID"
                        + " (HttpOnly), sem JWT. 1. GET /api/auth/csrf obtém cookie XSRF-TOKEN e"
                        + " token. 2. POST /api/auth/login com email/senha e header X-XSRF-TOKEN."
                        + " 3. Após login obtenha novamente /csrf. Envie o token em POST, PUT,"
                        + " PATCH e logout. Swagger usa cookies da mesma origem; não preencha"
                        + " JSESSIONID manualmente. Logout POST /api/auth/logout invalida a sessão."
                        + " 401: sessão ausente/inválida; 403: CSRF ou perfil. A clínica vem da"
                        + " sessão. Responsáveis só acessam pacientes vinculados e conteúdo"
                        + " compartilhado. Recursos inacessíveis retornam 404. Listagens: page"
                        + " começa em zero, size de 1 a 100 (padrão 50); resposta permanece array."
                        + " Nunca inserir dados reais neste ambiente."))
        .components(
            new Components()
                .addSecuritySchemes(
                    "sessao",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("JSESSIONID"))
                .addSecuritySchemes(
                    "csrf",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-XSRF-TOKEN")))
        .addSecurityItem(new SecurityRequirement().addList("sessao"));
  }

  @Bean
  OpenApiCustomizer detalhes() {
    return api -> {
      var schemas =
          new io.swagger.v3.core.converter.ModelConverters()
              .read(br.com.nutrikids.auth.Erros.ErroApi.class);
      schemas.forEach((name, schema) -> api.getComponents().addSchemas(name, schema));
      var exemplos =
          java.util.Map.ofEntries(
              java.util.Map.entry("nome", "Pessoa Sintética"),
              java.util.Map.entry("email", "pessoa@test.invalid"),
              java.util.Map.entry("nascimento", "2020-01-01"),
              java.util.Map.entry("sexo", "F"),
              java.util.Map.entry("observacoes", "Exemplo fictício para documentação"),
              java.util.Map.entry("conteudo", "Orientação exclusivamente sintética"),
              java.util.Map.entry("tipo", "DIARIO_ALIMENTAR"),
              java.util.Map.entry("pesoKg", 20),
              java.util.Map.entry("alturaCm", 110),
              java.util.Map.entry("posicao", "EM_PE"),
              java.util.Map.entry("titulo", "Arquivo sintético"),
              java.util.Map.entry("perfil", "RESPONSAVEL"));
      api.getComponents()
          .getSchemas()
          .forEach(
              (name, schema) -> {
                if (schema.getProperties() != null)
                  schema
                      .getProperties()
                      .forEach(
                          (field, propertyObject) -> {
                            var property = (Schema<?>) propertyObject;
                            if (property.getDescription() == null)
                              property.setDescription(
                                  "Campo " + field + " do contrato " + name + ".");
                            if (exemplos.containsKey(field))
                              property.setExample(exemplos.get(field));
                          });
              });
      api.getPaths()
          .forEach(
              (path, item) ->
                  item.readOperationsMap()
                      .forEach(
                          (method, op) -> {
                            boolean escrita = method != PathItem.HttpMethod.GET;
                            if (path.equals("/api/auth/csrf")) op.setSecurity(java.util.List.of());
                            else if (path.equals("/api/auth/login"))
                              op.setSecurity(
                                  java.util.List.of(new SecurityRequirement().addList("csrf")));
                            else
                              op.setSecurity(
                                  java.util.List.of(
                                      escrita
                                          ? new SecurityRequirement()
                                              .addList("sessao")
                                              .addList("csrf")
                                          : new SecurityRequirement().addList("sessao")));
                            for (String code :
                                new String[] {"400", "401", "403", "404", "409", "413", "429"}) {
                              String msg =
                                  switch (code) {
                                    case "400" ->
                                        "Entrada inválida; não inclui valores rejeitados.";
                                    case "401" ->
                                        "Sessão ausente/inválida ou credenciais incorretas.";
                                    case "403" ->
                                        "Perfil sem permissão ou token CSRF ausente/inválido.";
                                    case "404" -> "Recurso não encontrado no escopo autorizado.";
                                    case "409" ->
                                        "Conflito de integridade, versão concorrente ou agenda.";
                                    case "413" -> "Upload excede limite de 5 MB por requisição.";
                                    default -> "Limite de tentativas de login atingido.";
                                  };
                              // Security filter errors use the servlet error response; MVC errors
                              // use ErroApi.
                              var response = new ApiResponse().description(msg);
                              if (java.util.Set.of("400", "404", "409", "413", "429")
                                  .contains(code))
                                response.content(
                                    new Content()
                                        .addMediaType(
                                            "application/json",
                                            new MediaType()
                                                .schema(
                                                    new Schema<>()
                                                        .$ref("#/components/schemas/ErroApi"))));
                              op.getResponses().addApiResponse(code, response);
                            }
                            if (escrita)
                              op.addParametersItem(
                                  new Parameter()
                                      .name("X-XSRF-TOKEN")
                                      .in("header")
                                      .required(true)
                                      .description("Token de /api/auth/csrf; renovar após login.")
                                      .schema(new StringSchema()));
                          }));
      // Logout is handled by Spring Security, not by a controller.
      var logout =
          new Operation()
              .operationId("logout")
              .summary("Encerrar sessão")
              .addTagsItem("auth")
              .description("Invalida sessão e limpa contexto autenticado. Exige CSRF.")
              .addSecurityItem(new SecurityRequirement().addList("sessao").addList("csrf"))
              .responses(
                  new io.swagger.v3.oas.models.responses.ApiResponses()
                      .addApiResponse("204", new ApiResponse().description("Sessão encerrada."))
                      .addApiResponse(
                          "403", new ApiResponse().description("CSRF ausente/inválido.")));
      api.getPaths().addPathItem("/api/auth/logout", new PathItem().post(logout));
    };
  }
}
