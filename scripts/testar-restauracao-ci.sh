#!/usr/bin/env bash
# Somente no PostgreSQL descartável do job de CI; nunca usar banco de produção.
set -euo pipefail
psql -h 127.0.0.1 -U nutrikids_ci -d nutrikids_ci -v ON_ERROR_STOP=1 <<'SQL'
INSERT INTO clinica(id,nome) VALUES ('00000000-0000-0000-0000-000000000099','Restore sintético');
SQL
pg_dump -h 127.0.0.1 -U nutrikids_ci -Fc nutrikids_ci -f /tmp/nutrikids.dump
createdb -h 127.0.0.1 -U nutrikids_ci nutrikids_restore
pg_restore -h 127.0.0.1 -U nutrikids_ci --exit-on-error -d nutrikids_restore /tmp/nutrikids.dump
psql -h 127.0.0.1 -U nutrikids_ci -d nutrikids_restore -v ON_ERROR_STOP=1 <<'SQL'
DO $$
BEGIN
 IF (SELECT count(*) FROM clinica WHERE id='00000000-0000-0000-0000-000000000099') <> 1 THEN
   RAISE EXCEPTION 'Registro sintético não restaurado';
 END IF;
 IF (SELECT count(*) FROM flyway_schema_history WHERE success) <> 6 THEN
   RAISE EXCEPTION 'Histórico Flyway restaurado incompleto';
 END IF;
END $$;
SQL
