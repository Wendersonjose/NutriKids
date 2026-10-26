CREATE TABLE assinatura(clinica_id uuid PRIMARY KEY REFERENCES clinica(id),provedor_id varchar(255) NOT NULL UNIQUE,cliente_id varchar(255) NOT NULL,plano varchar(255) NOT NULL,valor numeric(14,2) NOT NULL CHECK(valor>0),status varchar(255) NOT NULL,versao bigint NOT NULL DEFAULT 0);
CREATE TABLE fatura(id varchar(255) PRIMARY KEY,clinica_id uuid NOT NULL REFERENCES assinatura(clinica_id),valor numeric(14,2) NOT NULL CHECK(valor>0),status varchar(255) NOT NULL,vencimento date NOT NULL);
CREATE INDEX fatura_clinica ON fatura(clinica_id,vencimento DESC);
CREATE TABLE webhook_processado(id varchar(255) PRIMARY KEY,recebido_em timestamptz NOT NULL);
