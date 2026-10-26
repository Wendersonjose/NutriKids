package br.com.nutrikids;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("postgres-test")
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = "jdbc:postgresql:.*")
class PostgresConcorrenciaTest {
  @Autowired DataSource ds;
  @Autowired JdbcTemplate jdbc;
  UUID clinica, usuario, paciente;

  @BeforeEach
  void dados() {
    clinica = UUID.randomUUID();
    usuario = UUID.randomUUID();
    paciente = UUID.randomUUID();
    jdbc.update("insert into clinica values (?,?)", clinica, "Auditoria sintética");
    jdbc.update(
        "insert into usuario(id,clinica_id,email,senha,nome,perfil) values"
            + " (?,?,?,?,?,'NUTRICIONISTA')",
        usuario,
        clinica,
        usuario + "@test.invalid",
        "hash-inutilizavel",
        "Sintético");
    jdbc.update(
        "insert into paciente(id,clinica_id,nome,nascimento,sexo) values"
            + " (?,?,'Sintético','2020-01-01','F')",
        paciente,
        clinica);
  }

  @AfterEach
  void limpar() {
    jdbc.update("delete from consulta where clinica_id=?", clinica);
    jdbc.update("delete from paciente where clinica_id=?", clinica);
    jdbc.update("delete from usuario where clinica_id=?", clinica);
    jdbc.update("delete from clinica where id=?", clinica);
  }

  String inserir(UUID id, UUID profissional, UUID p, String inicio, String fim) {
    return "insert into consulta(id,clinica_id,profissional_id,paciente_id,inicio,fim,status)"
        + " values ('"
        + id
        + "','"
        + clinica
        + "','"
        + profissional
        + "','"
        + p
        + "','"
        + inicio
        + "','"
        + fim
        + "','AGENDADA')";
  }

  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.ValueSource(strings = {"PACIENTE", "PROFISSIONAL"})
  void duasTransacoesDisputamMesmoHorario(String recurso) throws Exception {
    UUID profissional2 = usuario, paciente2 = paciente;
    if (recurso.equals("PACIENTE")) {
      profissional2 = UUID.randomUUID();
      jdbc.update(
          "insert into usuario(id,clinica_id,email,senha,nome,perfil) values"
              + " (?,?,?,?,?,'NUTRICIONISTA')",
          profissional2,
          clinica,
          profissional2 + "@test.invalid",
          "hash-inutilizavel",
          "Segundo sintético");
    } else {
      paciente2 = UUID.randomUUID();
      jdbc.update(
          "insert into paciente(id,clinica_id,nome,nascimento,sexo) values (?,?,'Segundo"
              + " sintético','2020-01-01','F')",
          paciente2,
          clinica);
    }
    final UUID segundoProfissional = profissional2, segundoPaciente = paciente2;
    var executor = Executors.newSingleThreadExecutor();
    var iniciou = new CountDownLatch(1);
    try (var primeira = ds.getConnection()) {
      primeira.setAutoCommit(false);
      primeira
          .createStatement()
          .execute(
              inserir(
                  UUID.randomUUID(),
                  usuario,
                  paciente,
                  "2026-12-01 10:00+00",
                  "2026-12-01 11:00+00"));
      var segunda =
          executor.submit(
              () -> {
                try (var conn = ds.getConnection()) {
                  conn.createStatement().execute("set statement_timeout='8s'");
                  iniciou.countDown();
                  conn.createStatement()
                      .execute(
                          inserir(
                              UUID.randomUUID(),
                              segundoProfissional,
                              segundoPaciente,
                              "2026-12-01 10:30+00",
                              "2026-12-01 11:30+00"));
                  return "ACEITA";
                } catch (java.sql.SQLException e) {
                  return e.getSQLState();
                }
              });
      assertTrue(iniciou.await(5, TimeUnit.SECONDS));
      primeira.commit();
      assertEquals("23P01", segunda.get(10, TimeUnit.SECONDS));
      assertEquals(
          1,
          jdbc.queryForObject(
              "select count(*) from consulta where clinica_id=?", Integer.class, clinica));
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void intervalosAdjacentesPermitidosEDuracaoMaximaExata() {
    jdbc.execute(
        inserir(
            UUID.randomUUID(), usuario, paciente, "2026-12-01 10:00+00", "2026-12-01 18:00+00"));
    jdbc.execute(
        inserir(
            UUID.randomUUID(), usuario, paciente, "2026-12-01 18:00+00", "2026-12-01 19:00+00"));
    assertThrows(
        org.springframework.dao.DataIntegrityViolationException.class,
        () ->
            jdbc.execute(
                inserir(
                    UUID.randomUUID(),
                    usuario,
                    paciente,
                    "2026-12-02 10:00+00",
                    "2026-12-02 18:00:00.001+00")));
  }

  @Test
  void chavesCompostasBloqueiamOutraClinica() {
    var outra = UUID.randomUUID();
    var outroPaciente = UUID.randomUUID();
    jdbc.update("insert into clinica values (?,?)", outra, "Outra sintética");
    jdbc.update(
        "insert into paciente(id,clinica_id,nome,nascimento,sexo) values"
            + " (?,?,'Sintético','2020-01-01','F')",
        outroPaciente,
        outra);
    try {
      assertThrows(
          org.springframework.dao.DataIntegrityViolationException.class,
          () ->
              jdbc.execute(
                  inserir(
                      UUID.randomUUID(),
                      usuario,
                      outroPaciente,
                      "2026-12-01 10:00+00",
                      "2026-12-01 11:00+00")));
      assertEquals(
          2,
          jdbc.queryForObject(
              "select count(*) from pg_constraint where conrelid='consulta'::regclass and"
                  + " contype='x'",
              Integer.class));
      assertTrue(
          jdbc.queryForObject(
                  "select count(*) from pg_indexes where tablename in"
                      + " ('consulta','paciente','vinculo','registro','anexo','medida','usuario')",
                  Integer.class)
              >= 10);
    } finally {
      jdbc.update("delete from paciente where id=?", outroPaciente);
      jdbc.update("delete from clinica where id=?", outra);
    }
  }
}
