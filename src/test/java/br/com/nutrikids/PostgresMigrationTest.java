package br.com.nutrikids;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("postgres-test")
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = "jdbc:postgresql:.*")
@Transactional
class PostgresMigrationTest {
  @Autowired JdbcTemplate jdbc;

  @Test
  void migraEsquemaERejeitaSobreposicao() {
    UUID c = UUID.randomUUID(), u = UUID.randomUUID(), p = UUID.randomUUID();
    jdbc.update("insert into clinica(id,nome) values (?,?)", c, "Teste sintético");
    jdbc.update(
        "insert into usuario(id,clinica_id,email,senha,nome,perfil) values (?,?,?,?,?,?)",
        u,
        c,
        UUID.randomUUID() + "@test.invalid",
        "hash-sintetico-inutilizavel",
        "Sintético",
        "NUTRICIONISTA");
    jdbc.update(
        "insert into paciente(id,clinica_id,nome,nascimento,sexo) values (?,?,?,'2020-01-01','F')",
        p,
        c,
        "Paciente sintético");
    String sql =
        "insert into consulta(id,clinica_id,paciente_id,profissional_id,inicio,fim,status) values"
            + " (?,?,?,?,'2026-12-01 10:00:00+00','2026-12-01 11:00:00+00','AGENDADA')";
    jdbc.update(sql, UUID.randomUUID(), c, p, u);
    assertThrows(
        DataIntegrityViolationException.class, () -> jdbc.update(sql, UUID.randomUUID(), c, p, u));
  }
}
