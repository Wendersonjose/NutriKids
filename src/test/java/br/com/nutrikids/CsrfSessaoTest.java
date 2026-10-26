package br.com.nutrikids;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.nutrikids.clinica.*;
import br.com.nutrikids.paciente.*;
import br.com.nutrikids.responsavel.*;
import br.com.nutrikids.usuario.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

@SpringBootTest(
    properties = {
      "BOOTSTRAP_EMAIL=csrf@test.invalid",
      "BOOTSTRAP_PASSWORD=SenhaSintetica123!",
      "spring.datasource.url=jdbc:h2:mem:csrf_real;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
    })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CsrfSessaoTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @Test
  void csrfRealSessaoRenovadaELogoutProtegido() throws Exception {
    var response =
        mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn().getResponse();
    var cookie = response.getCookie("XSRF-TOKEN");
    assertNotNull(cookie);
    String token = json.readTree(response.getContentAsString()).get("token").asText();
    var antiga = new MockHttpSession();
    var login =
        mvc.perform(
                post("/api/auth/login")
                    .session(antiga)
                    .cookie(cookie)
                    .header("X-XSRF-TOKEN", token)
                    .contentType("application/json")
                    .content("{\"email\":\"csrf@test.invalid\",\"senha\":\"SenhaSintetica123!\"}"))
            .andExpect(status().isOk())
            .andReturn();
    assertTrue(antiga.isInvalid());
    var atual = (MockHttpSession) login.getRequest().getSession(false);
    mvc.perform(get("/api/auth/me").session(atual)).andExpect(status().isOk());
    mvc.perform(post("/api/auth/logout").session(atual)).andExpect(status().isForbidden());
    assertFalse(atual.isInvalid());
    var novo =
        mvc.perform(get("/api/auth/csrf").session(atual))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    var novoCookie = novo.getCookie("XSRF-TOKEN");
    assertNotNull(novoCookie);
    String novoToken = json.readTree(novo.getContentAsString()).get("token").asText();
    mvc.perform(
            post("/api/auth/logout")
                .session(atual)
                .cookie(novoCookie)
                .header("X-XSRF-TOKEN", novoToken))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
  }
}
