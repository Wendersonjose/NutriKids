package br.com.nutrikids;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.ActiveProfiles;

/** Executa os mesmos fluxos e testes negativos contra PostgreSQL real, sem H2/Docker. */
@ActiveProfiles(value = "postgres-test", inheritProfiles = false)
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = "jdbc:postgresql:.*")
class PostgresFluxoClinicoTest extends FluxoClinicoTest {}
