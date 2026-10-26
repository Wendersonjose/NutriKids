package br.com.nutrikids;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class OpenApiTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @Autowired
  org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mappings;

  @Test
  void documentacaoCompletaSemDados() throws Exception {
    var raw =
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    var api = mapper.readTree(raw);
    assertTrue(api.get("paths").has("/api/auth/logout"));
    mappings
        .getHandlerMethods()
        .forEach(
            (mapping, handler) -> {
              if (!handler.getBeanType().getPackageName().startsWith("br.com.nutrikids")) return;
              for (String path : mapping.getPatternValues())
                for (var method : mapping.getMethodsCondition().getMethods()) {
                  assertTrue(api.get("paths").has(path), path);
                  assertTrue(
                      api.get("paths")
                          .get(path)
                          .has(method.name().toLowerCase(java.util.Locale.ROOT)),
                      path + method);
                }
            });
    assertFalse(
        api.get("components")
            .get("schemas")
            .has("org.springframework.security.web.csrf.CsrfToken"));
    assertTrue(api.get("components").get("securitySchemes").has("csrf"));
    assertFalse(raw.contains("Informação clínica privada"));
    for (var paths = api.get("paths").elements(); paths.hasNext(); ) {
      var path = paths.next();
      for (var operations = path.fields(); operations.hasNext(); ) {
        var op = operations.next();
        if (!java.util.Set.of("get", "post", "put", "patch").contains(op.getKey())) continue;
        assertTrue(op.getValue().has("summary"), op.toString());
        assertTrue(op.getValue().has("responses"));
      }
    }
    var yaml =
        mvc.perform(get("/v3/api-docs.yaml"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
    Files.createDirectories(Path.of("target"));
    Files.write(Path.of("target/openapi.yaml"), yaml);
    mvc.perform(get("/swagger-ui/index.html"))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    "Content-Security-Policy",
                    org.hamcrest.Matchers.containsString("script-src 'self'")));
    // Documentation chain must never authorize API resources.
    mvc.perform(get("/api/pacientes")).andExpect(status().isUnauthorized());
    mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
  }
}
