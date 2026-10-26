# PostgreSQL local no Windows, sem Docker

Instale PostgreSQL 16+ pelo instalador oficial e mantenha acesso somente local. No pgAdmin/psql, conectado como administrador:

```sql
CREATE ROLE nutri_kids_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE;
```

No psql, defina a senha de modo interativo, sem incluí-la no histórico SQL:

```text
\password nutri_kids_app
```

Depois:

```sql
CREATE DATABASE nutri_kids OWNER nutri_kids_app;
```

Conecte ao banco nutri_kids como administrador e prepare a extensão usada pela agenda:

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;
```

Defina DB_URL, DB_USERNAME e DB_PASSWORD no PowerShell. Flyway aplica V1 e V2 ao iniciar. Não executar manualmente o conteúdo das migrações e depois habilitar Flyway no mesmo banco. Hibernate valida a estrutura e não cria tabelas na execução normal.

O usuário inicial aplica migrações e executa a API nesta configuração local. Para produção, separar papel migrador (DDL) do papel de execução (DML), restringir privilégios e testar as permissões. Não executar como postgres/superusuário.

## Verificações obrigatórias no PostgreSQL

1. Inicialização em banco vazio com Flyway e Hibernate validate.
2. Reinicialização sem reaplicar versões.
3. Duas consultas sobrepostas do mesmo profissional: segunda deve retornar 409.
4. Duas requisições concorrentes sobrepostas: apenas uma deve ser aceita.
5. Consultas adjacentes devem ser aceitas; cancelada libera horário.
6. FK composta deve impedir vínculo cruzado de clínicas.
7. Backup e restauração de pacientes, histórico e anexos.

A suite H2 não valida essas características específicas do PostgreSQL. Consulte validacao.md para saber quais foram executadas neste ambiente.
