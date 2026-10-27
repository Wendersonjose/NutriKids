package br.com.nutrikids;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FluxoClinicoTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UsuarioRepository users;
  @Autowired ClinicaRepository clinics;
  @Autowired PacienteRepository patients;
  @Autowired VinculoRepository links;
  @Autowired PasswordEncoder encoder;
  UUID clinic, other, admin, family, patient, foreignPatient;

  @BeforeEach
  void preparar() {
    clinic = UUID.randomUUID();
    other = UUID.randomUUID();
    clinics.save(new Clinica(clinic, "Clínica teste"));
    clinics.save(new Clinica(other, "Outra clínica"));
    admin = usuario("admin@test.invalid", "ADMIN", clinic);
    family = usuario("familia@test.invalid", "RESPONSAVEL", clinic);
    patient = paciente(clinic);
    foreignPatient = paciente(other);
  }

  UUID usuario(String email, String perfil, UUID c) {
    var u = new Usuario();
    u.id = UUID.randomUUID();
    u.clinicaId = c;
    u.email = email;
    u.nome = "Usuário sintético";
    u.perfil = perfil;
    u.senha = encoder.encode("SenhaSintetica123!");
    users.save(u);
    return u.id;
  }

  UUID paciente(UUID c) {
    var p = new Paciente();
    p.id = UUID.randomUUID();
    p.clinicaId = c;
    p.nome = "Paciente sintético";
    p.nascimento = java.time.LocalDate.of(2020, 1, 1);
    p.sexo = "F";
    p.observacoes = "Informação clínica privada";
    patients.save(p);
    return p.id;
  }

  MockHttpSession login(String email) throws Exception {
    return (MockHttpSession)
        mvc.perform(
                post("/api/auth/login")
                    .with(
                        r -> {
                          r.setRemoteAddr(UUID.randomUUID().toString());
                          return r;
                        })
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json.writeValueAsString(
                            Map.of("email", email, "senha", "SenhaSintetica123!"))))
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);
  }

  @Test
  void exigeAutenticacao() throws Exception {
    mvc.perform(get("/api/pacientes")).andExpect(status().isUnauthorized());
  }

  @Test
  void exigeCsrfNoLogin() throws Exception {
    mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejeitaSenhaIncorreta() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .with(
                    r -> {
                      r.setRemoteAddr(UUID.randomUUID().toString());
                      return r;
                    })
                .with(csrf())
                .contentType("application/json")
                .content("{\"email\":\"admin@test.invalid\",\"senha\":\"errada\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void isolaClinicas() throws Exception {
    var s = login("admin@test.invalid");
    mvc.perform(get("/api/pacientes/" + foreignPatient).session(s))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/pacientes").session(s)).andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void responsavelPrecisaVinculo() throws Exception {
    var s = login("familia@test.invalid");
    mvc.perform(get("/api/pacientes/" + patient).session(s)).andExpect(status().isNotFound());
    mvc.perform(get("/api/pacientes").session(s)).andExpect(jsonPath("$.length()").value(0));
  }

  void vincular() {
    var v = new Vinculo();
    v.id = UUID.randomUUID();
    v.clinicaId = clinic;
    v.pacienteId = patient;
    v.usuarioId = family;
    v.parentesco = "Responsável";
    links.save(v);
  }

  @Test
  void responsavelNaoVeObservacoesPrivadas() throws Exception {
    vincular();
    var s = login("familia@test.invalid");
    mvc.perform(get("/api/pacientes/" + patient).session(s))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.observacoes").isEmpty());
  }

  @Test
  void responsavelNaoCriaPacienteNemVeEquipe() throws Exception {
    var s = login("familia@test.invalid");
    mvc.perform(get("/api/equipe").session(s)).andExpect(status().isForbidden());
    mvc.perform(
            post("/api/pacientes")
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content("{\"nome\":\"Sintético\",\"nascimento\":\"2020-01-01\",\"sexo\":\"F\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void fluxoPacienteRegistroMedidaRelatorio() throws Exception {
    var s = login("admin@test.invalid");
    mvc.perform(
            post("/api/pacientes/" + patient + "/registros")
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"tipo\":\"ANAMNESE\",\"conteudo\":\"Sem dados"
                        + " reais.\",\"compartilhado\":false}"))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/pacientes/" + patient + "/medidas")
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"data\":\"2026-01-01\",\"pesoKg\":20,\"alturaCm\":110,\"posicao\":\"EM_PE\"}"))
        .andExpect(status().isCreated());
    var pdf =
        mvc.perform(get("/api/pacientes/" + patient + "/relatorio.pdf").session(s))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/pdf"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
    assertEquals("%PDF", new String(pdf, 0, 4));
  }

  @Test
  void familiaVeSomenteCompartilhados() throws Exception {
    var a = login("admin@test.invalid");
    for (boolean shared : List.of(false, true)) {
      mvc.perform(
              post("/api/pacientes/" + patient + "/registros")
                  .session(a)
                  .with(csrf())
                  .contentType("application/json")
                  .content(
                      json.writeValueAsString(
                          Map.of(
                              "tipo",
                              "PLANO_ALIMENTAR",
                              "conteudo",
                              "Orientação sintética",
                              "compartilhado",
                              shared))))
          .andExpect(status().isCreated());
    }
    vincular();
    var f = login("familia@test.invalid");
    mvc.perform(get("/api/pacientes/" + patient + "/registros").session(f))
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(
            post("/api/pacientes/" + patient + "/registros")
                .session(f)
                .with(csrf())
                .contentType("application/json")
                .content("{\"tipo\":\"PRONTUARIO\",\"conteudo\":\"Tentativa\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void validaMedidaEData() throws Exception {
    var s = login("admin@test.invalid");
    mvc.perform(
            post("/api/pacientes/" + patient + "/medidas")
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"data\":\"2019-01-01\",\"pesoKg\":20,\"alturaCm\":110,\"posicao\":\"EM_PE\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void agendaConfereProfissionalDaClinica() throws Exception {
    var s = login("admin@test.invalid");
    var u = usuario("outro@test.invalid", "NUTRICIONISTA", other);
    mvc.perform(
            post("/api/agenda")
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json.writeValueAsString(
                        Map.of(
                            "pacienteId",
                            patient,
                            "profissionalId",
                            u,
                            "inicio",
                            "2026-12-01T10:00:00-03:00",
                            "fim",
                            "2026-12-01T11:00:00-03:00"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void agendaCriaConcluiEImpedeNovaTransicao() throws Exception {
    var s = login("admin@test.invalid");
    var response =
        mvc.perform(
                post("/api/agenda")
                    .session(s)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json.writeValueAsString(
                            Map.of(
                                "pacienteId",
                                patient,
                                "profissionalId",
                                admin,
                                "inicio",
                                "2026-12-01T10:00:00-03:00",
                                "fim",
                                "2026-12-01T11:00:00-03:00"))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    var id = json.readTree(response).get("id").asText();
    mvc.perform(
            patch("/api/agenda/" + id)
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content("{\"status\":\"CONCLUIDA\"}"))
        .andExpect(status().isOk());
    mvc.perform(
            patch("/api/agenda/" + id)
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content("{\"status\":\"CANCELADA\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void logoutRevogaSessao() throws Exception {
    var s = login("admin@test.invalid");
    mvc.perform(post("/api/auth/logout").session(s).with(csrf())).andExpect(status().isNoContent());
    assertTrue(s.isInvalid());
  }

  @Test
  void anexoProtegidoPorPacienteECompartilhamento() throws Exception {
    var a = login("admin@test.invalid");
    var file =
        new org.springframework.mock.web.MockMultipartFile(
            "arquivo", "sintetico.pdf", "application/pdf", "%PDF-1.7 sintetico".getBytes());
    var response =
        mvc.perform(
                multipart("/api/pacientes/" + patient + "/anexos")
                    .file(file)
                    .param("titulo", "Exame sintético")
                    .session(a)
                    .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.conteudo").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id = json.readTree(response).get("id").asText();
    mvc.perform(get("/api/pacientes/" + patient + "/anexos/" + id).session(a))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    vincular();
    var f = login("familia@test.invalid");
    mvc.perform(get("/api/pacientes/" + patient + "/anexos/" + id).session(f))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/pacientes/" + patient + "/anexos").session(f))
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void anexoRejeitaTipoForjado() throws Exception {
    var a = login("admin@test.invalid");
    var file =
        new org.springframework.mock.web.MockMultipartFile(
            "arquivo", "falso.pdf", "application/pdf", "<script>fake</script>".getBytes());
    mvc.perform(
            multipart("/api/pacientes/" + patient + "/anexos")
                .file(file)
                .param("titulo", "Inválido")
                .session(a)
                .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void familiaEnviaArquivoEClinicaRecebe() throws Exception {
    vincular();
    var f = login("familia@test.invalid");
    var file =
        new org.springframework.mock.web.MockMultipartFile(
            "arquivo", "sintetico.pdf", "application/pdf", "%PDF-1.7 sintetico".getBytes());
    mvc.perform(
            multipart("/api/pacientes/" + patient + "/anexos")
                .file(file)
                .param("titulo", "Arquivo da família")
                .session(f)
                .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.compartilhado").value(true));
    var a = login("admin@test.invalid");
    mvc.perform(get("/api/pacientes/" + patient + "/anexos").session(a))
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void criacaoDeUsuarioNaoExibeHash() throws Exception {
    var a = login("admin@test.invalid");
    mvc.perform(
            post("/api/equipe")
                .session(a)
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"nome\":\"Responsável"
                        + " sintético\",\"email\":\"novo@test.invalid\",\"senha\":\"SomenteTeste123!\",\"perfil\":\"RESPONSAVEL\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.senha").doesNotExist());
  }

  @Test
  void nutricionistaNaoCriaAcessoAdmin() throws Exception {
    usuario("nutri@test.invalid", "NUTRICIONISTA", clinic);
    var a = login("nutri@test.invalid");
    mvc.perform(
            post("/api/equipe")
                .session(a)
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"nome\":\"Tentativa\",\"email\":\"tentativa@test.invalid\",\"senha\":\"SomenteTeste123!\",\"perfil\":\"ADMIN\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void respostaClinicaNaoPermiteCache() throws Exception {
    var a = login("admin@test.invalid");
    mvc.perform(get("/api/pacientes").session(a))
        .andExpect(header().string("Cache-Control", "no-store"));
  }

  @Autowired br.com.nutrikids.gamificacao.PartidaRepository partidas;

  @Test
  void financeiroIsolaClinicasELiquidaUmaVez() throws Exception {
    var session = login("admin@test.invalid");
    String body =
        "{\"tipo\":\"RECEITA\",\"descricao\":\"Consulta"
            + " sintética\",\"categoria\":\"Consulta\",\"conta\":\"Caixa\",\"valor\":123.45,\"competencia\":\"2026-10-01\",\"vencimento\":\"2026-10-02\"}";
    var r =
        mvc.perform(
                post("/api/financeiro")
                    .session(session)
                    .with(csrf())
                    .contentType("application/json")
                    .content(body))
            .andExpect(status().isCreated())
            .andReturn();
    String id = json.readTree(r.getResponse().getContentAsString()).get("id").asText();
    String baixa = "{\"pagamento\":\"2026-10-01\",\"referencia\":\"extrato-sintetico\"}";
    for (int n = 0; n < 2; n++)
      mvc.perform(
              post("/api/financeiro/" + id + "/liquidar")
                  .session(session)
                  .with(csrf())
                  .contentType("application/json")
                  .content(baixa))
          .andExpect(status().isOk());
    mvc.perform(get("/api/financeiro/resumo?inicio=2026-10-01&fim=2026-10-31").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.receitasLiquidadas").value(123.45));
    usuario("outra@test.invalid", "ADMIN", other);
    mvc.perform(
            post("/api/financeiro/" + id + "/liquidar")
                .session(login("outra@test.invalid"))
                .with(csrf())
                .contentType("application/json")
                .content(baixa))
        .andExpect(status().isNotFound());
    mvc.perform(
            get("/api/financeiro?inicio=2026-10-01&fim=2026-10-31")
                .session(login("familia@test.invalid")))
        .andExpect(status().isForbidden());
  }

  @Test
  void financeiroRejeitaPrecisaoInvalidaENutricionista() throws Exception {
    usuario("nutri-fin@test.invalid", "NUTRICIONISTA", clinic);
    mvc.perform(
            get("/api/financeiro?inicio=2026-10-01&fim=2026-10-31")
                .session(login("nutri-fin@test.invalid")))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/financeiro")
                .session(login("admin@test.invalid"))
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"tipo\":\"RECEITA\",\"descricao\":\"Teste\",\"categoria\":\"Teste\",\"conta\":\"Caixa\",\"valor\":1.234,\"competencia\":\"2026-10-01\",\"vencimento\":\"2026-10-02\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void alimentoCalculaMedidaENaoVazaOutraClinica() throws Exception {
    var session = login("admin@test.invalid");
    var r =
        mvc.perform(
                post("/api/alimentos")
                    .session(session)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        "{\"nome\":\"Alimento"
                            + " sintético\",\"fonte\":\"Teste\",\"versaoFonte\":\"1\",\"permissaoUso\":\"Dado"
                            + " sintético\",\"nutrientes\":{\"energia_kcal\":100},\"medidas\":{\"colher\":25}}"))
            .andExpect(status().isCreated())
            .andReturn();
    String id = json.readTree(r.getResponse().getContentAsString()).get("id").asText();
    String body =
        "{\"itens\":[{\"alimentoId\":\"" + id + "\",\"quantidade\":2,\"medida\":\"colher\"}]}";
    mvc.perform(
            post("/api/alimentos/calcular")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totais.energia_kcal").value(50));
    mvc.perform(
            post("/api/pacientes/" + patient + "/plano-calculado")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"titulo\":\"Plano"
                        + " sintético\",\"compartilhado\":true,\"refeicoes\":[{\"nome\":\"Almoço\",\"itens\":[{\"alimentoId\":\""
                        + id
                        + "\",\"quantidade\":50}]}]}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.tipo").value("PLANO_ALIMENTAR"));
    usuario("alimento-outra@test.invalid", "ADMIN", other);
    mvc.perform(
            post("/api/alimentos/calcular")
                .session(login("alimento-outra@test.invalid"))
                .with(csrf())
                .contentType("application/json")
                .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  void jogoCompletaPersistindoConquistaESemExporTabuleiro() throws Exception {
    var session = login("admin@test.invalid");
    String path = "/api/pacientes/" + patient + "/jogos/memoria";
    var r =
        mvc.perform(post(path).session(session).with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tabuleiro").doesNotExist())
            .andReturn();
    UUID id =
        UUID.fromString(json.readTree(r.getResponse().getContentAsString()).get("id").asText());
    String[] cartas = partidas.findById(id).orElseThrow().tabuleiro.split(",");
    for (String alimento : new HashSet<>(Arrays.asList(cartas))) {
      var indices = new ArrayList<Integer>();
      for (int n = 0; n < cartas.length; n++) if (cartas[n].equals(alimento)) indices.add(n);
      mvc.perform(
              post(path + "/" + id + "/jogadas")
                  .session(session)
                  .with(csrf())
                  .contentType("application/json")
                  .content(
                      json.writeValueAsString(
                          Map.of("primeira", indices.get(0), "segunda", indices.get(1)))))
          .andExpect(status().isOk());
    }
    mvc.perform(get(path).session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].concluida").value(true));
    mvc.perform(get(path).session(login("familia@test.invalid"))).andExpect(status().isNotFound());
    mvc.perform(get("/api/pacientes/" + foreignPatient + "/jogos/memoria").session(session))
        .andExpect(status().isNotFound());
  }

  @Test
  void crescimentoNaoAceitaPacienteDeOutraClinica() throws Exception {
    var s = login("admin@test.invalid");
    mvc.perform(get("/api/pacientes/" + foreignPatient + "/crescimento?edema=false").session(s))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/pacientes/" + patient + "/crescimento").session(s))
        .andExpect(status().isBadRequest());
  }

  @Test
  void privacidadeExigeVinculoERespostaAdministrativa() throws Exception {
    var adminSession = login("admin@test.invalid");
    String path = "/api/pacientes/" + patient + "/privacidade/solicitacoes";
    mvc.perform(
            post(path)
                .session(login("familia@test.invalid"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"tipo\":\"EXCLUSAO\"}"))
        .andExpect(status().isNotFound());
    var r =
        mvc.perform(
                post(path)
                    .session(adminSession)
                    .with(csrf())
                    .contentType("application/json")
                    .content("{\"tipo\":\"ACESSO\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    String id = json.readTree(r.getResponse().getContentAsString()).get("id").asText();
    mvc.perform(
            post(path + "/" + id + "/resposta")
                .session(adminSession)
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"status\":\"CONCLUIDA\",\"resposta\":\"Identidade conferida e exportação"
                        + " entregue por canal seguro (teste sintético)\"}"))
        .andExpect(status().isOk());
    mvc.perform(
            get("/api/pacientes/" + foreignPatient + "/privacidade/solicitacoes")
                .session(adminSession))
        .andExpect(status().isNotFound());
  }

  @Test
  void webhookSemTokenNaoExigeSessaoMasRecusaEvento() throws Exception {
    mvc.perform(post("/api/webhooks/asaas-sandbox").contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized());
  }
}
