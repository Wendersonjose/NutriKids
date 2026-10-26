package br.com.nutrikids;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev", "prod"})
class SwaggerProducaoTest {
  @Autowired MockMvc mvc;

  @Test
  void nemAdminPodeAcessarDocumentacaoEmProducao() throws Exception {
    for (String path : new String[] {"/swagger-ui/index.html", "/v3/api-docs", "/v3/api-docs.yaml"})
      mvc.perform(get(path).with(user("sintetico").roles("ADMIN")))
          .andExpect(status().isForbidden());
  }
}
