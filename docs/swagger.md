# Swagger / OpenAPI

Java 17, Spring Boot 3.5.16 e springdoc-openapi-starter-webmvc-ui 2.10.0.
Consulta oficial em 26/10/2026: https://springdoc.org/v2/ (matriz 3.5.x → 2.8.x–2.10.x; versão estável indicada 2.10.0). A FAQ antiga em /v2/faq.html ainda indica 2.9.1: prevalece a página atual, e a compatibilidade é verificada pelo build e teste de geração.

## Iniciar em desenvolvimento

Configure DB_URL, DB_USERNAME e DB_PASSWORD para seu banco **de desenvolvimento** conforme README. PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
mvn spring-boot:run
```

- Interface: http://localhost:8080/swagger-ui/index.html
- JSON: http://localhost:8080/v3/api-docs
- YAML: http://localhost:8080/v3/api-docs.yaml
- Contrato versionado: [openapi.yaml](openapi.yaml)

Sem dev, documentação desabilitada e rotas negadas pela segurança. A configuração de Swagger usa `@Profile("dev & !prod")`; prod nega acesso mesmo se dev estiver também ativo. Não há liberação de `/api/**`. A política CSP que permite scripts/estilos locais aplica-se exclusivamente aos recursos da documentação em dev; respostas clínicas mantêm CSP restritiva. Não habilitar dev em uma instância com dados reais.

## Sessão e CSRF

1. Execute GET /api/auth/csrf. O navegador recebe cookie XSRF-TOKEN e o JSON contém token.
2. Execute POST /api/auth/login com email/senha sintéticos e header X-XSRF-TOKEN correspondente ao cookie.
3. A sessão é substituída; JSESSIONID é HttpOnly. Consulte /csrf novamente antes das próximas mutações.
4. O Swagger opera na mesma origem; cookies são enviados pelo navegador. A opção `springdoc.swagger-ui.csrf.enabled` lê XSRF-TOKEN e envia X-XSRF-TOKEN. Não tente inserir JSESSIONID manualmente no botão Authorize.
5. GET /api/auth/me confirma a sessão. POST /api/auth/logout com CSRF encerra-a (204). Não existe token JWT ou armazenamento de senha no navegador.

401 significa sessão ausente/inválida ou credenciais incorretas; 403 indica CSRF/perfil. Recursos fora da clínica ou sem vínculo são 404. Erros MVC têm `{mensagem, campos}` sem valores rejeitados; erros do filtro de segurança seguem a resposta de erro do servlet. Não se promete o mesmo corpo para erros de filtro/proxy.

Exemplos, UUIDs e textos do contrato são sintéticos; o gerador inspeciona assinaturas e annotations e não consulta pacientes. `senha` só existe como entrada writeOnly. DTOs de anexos não incluem bytea; downloads exigem autorização específica.

## Paginação e filtros

Todas as listagens de pacientes, equipe, agenda, registros, medidas e anexos aceitam page (zero a 100000) e size (1 a 100, padrão 50). Respostas continuam arrays para manter o formato atual; use páginas sucessivas até uma página incompleta. Não há total embutido no array. Clientes devem oferecer navegação para não exibir apenas os primeiros 50 itens.

Pacientes aceitam nome (trecho sem distinguir maiúsculas; `%` e `_` tratados literalmente). Agenda aceita profissionalId, inicio e fim (início da consulta >= inicio e < fim). Ordenação estável com id como desempate. Registros/anexos: mais recentes primeiro; medidas: data crescente. Filtros de clínica e compartilhamento são aplicados no SQL antes da paginação.

## Reexportar sem dados reais

```powershell
mvn -Dtest=OpenApiTest test
Copy-Item target/openapi.yaml docs/openapi.yaml
```

O teste usa H2 vazio e mocks HTTP, valida summaries, esquemas de segurança, rotas de UI/JSON/YAML e preservação do bloqueio da API. O resultado exportado só deve ser copiado após sucesso. O contrato inclui logout, implementado pelo filtro de segurança e ausente do scan de controllers.
