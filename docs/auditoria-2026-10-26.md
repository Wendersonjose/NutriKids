# Auditoria de arquitetura e Swagger — 26/10/2026

Base: main `6c9e719143889890f635bcd6f1088e1167eb7a36`.
Branch: feat/auditoria-arquitetura-openapi.

## Escopo e achados

- Inspeção de todos os controllers, entidades, repositórios, autenticação/autorização, migrations V1/V2 e testes existentes.
- Controllers continham regras e retornavam entidades; services e DTOs explícitos introduzidos.
- Duração da agenda truncada em horas; corrigida comparação exata e incluído CHECK V3.
- Listas sem limites; paginação SQL e filtros implementados, sem mudar arrays para Page.
- Migrations já protegiam vínculos entre clínicas e sobreposição por paciente/profissional. Preservadas.
- @Version já existia em Paciente/Consulta; conflitos agora mapeados para 409.
- Uploads já tinham assinatura inicial, limite, autorização, attachment e nosniff. Não há antivírus; limitação mantida e documentada.
- Teste de PDF passou a conferir o texto, incluindo ausência de registro privado para responsável.
- Documentação gerada em contexto sintético; nenhum conteúdo de paciente é usado para exemplos.
- Backend apenas: frontend está em outro repositório e não foi modificado.

## PostgreSQL e ambiente

O ambiente local oferece somente identidade root e impede mudança de UID. Download isolado de PostgreSQL 16 foi possível, mas execução normal como usuário sem privilégios ficou bloqueada (`setuid`/user namespace não permitidos). Não foram removidas verificações do PostgreSQL, nem usados bancos de outros projetos.

O CI usa serviço PostgreSQL local do runner Ubuntu 24.04, banco exclusivo `nutrikids_auditoria_test`, usuário descartável e dados sintéticos; não usa Docker. TEST_DB_URL habilita PostgresMigrationTest e PostgresConcorrenciaTest, que verificam Flyway + validate JPA, sobreposição concorrente em conexões separadas, SQLSTATE 23P01, intervalos adjacentes, duração exata, FK entre clínicas e presença de exclusões/índices. Resultado remoto deve ser conferido no PR.

Para executar localmente com banco separado já criado:

```powershell
$env:TEST_DB_URL = "jdbc:postgresql://localhost:5432/nutrikids_auditoria_test"
$env:TEST_DB_USERNAME = "nutrikids_test"
$dbSecret = Read-Host "Senha do banco de teste" -AsSecureString
$env:TEST_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $dbSecret).Password
mvn verify
```

Não apontar TEST_DB_URL para banco compartilhado/produção: Flyway altera o schema e os testes gravam fixtures descartáveis. Requer permissão para criar a extensão btree_gist nesse banco.

## Resultado local executado

Comando: `mvn verify` com Maven 3.9.11 e Java 17.0.20; finalizado em 26/10/2026 às 20:57:59 (Brasília). **BUILD SUCCESS**.

| Suíte | Casos | Falhas | Erros | Ignorados |
|---|---:|---:|---:|---:|
| br.com.nutrikids.CsrfSessaoTest | 1 | 0 | 0 | 0 |
| br.com.nutrikids.FluxoClinicoTest | 22 | 0 | 0 | 0 |
| br.com.nutrikids.IsolamentoUnitTest | 1 | 0 | 0 | 0 |
| br.com.nutrikids.OpenApiTest | 1 | 0 | 0 | 0 |
| br.com.nutrikids.PostgresConcorrenciaTest | 3 | 0 | 0 | 3 |
| br.com.nutrikids.PostgresMigrationTest | 1 | 0 | 0 | 1 |
| br.com.nutrikids.SwaggerProducaoTest | 1 | 0 | 0 | 0 |
| br.com.nutrikids.auth.ProtecaoLoginTest | 1 | 0 | 0 | 0 |

27 testes executados e aprovados; 4 casos PostgreSQL ignorados localmente por ausência de TEST_DB_URL. Os casos parametrizados de concorrência só são expandidos quando o perfil PostgreSQL é habilitado. Isso não comprova Flyway, validate JPA ou concorrência PostgreSQL. O contrato foi exportado de OpenApiTest aprovado, sem consultas a dados reais. `git diff --check` também executado sem erros.
