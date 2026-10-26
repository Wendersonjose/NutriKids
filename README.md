# NutriKids — Backend

Núcleo funcional de acompanhamento de nutrição infantil com identidade e código próprios. Java 17, Spring Boot 3.5.16, Spring Security, JPA, PostgreSQL, Flyway, Maven, React TypeScript, Vite e React Router. Sem Docker.

**Entrega incremental; não é uma plataforma completa ou pronta para dados clínicos reais.** Consulte [status](docs/status.md), [validação](docs/validacao.md), [arquitetura](docs/arquitetura.md) e [pesquisa pública](docs/referencia-pequenutri/fontes.md).

## Executar no Windows

Pré-requisitos: JDK 17, Maven 3.9+, Node.js 22.12+ LTS, npm, Git e PostgreSQL 16+. Confirme `java -version`, `mvn -version`, `node -v`. Crie banco e usuário seguindo [PostgreSQL Windows](docs/postgresql-windows.md).

Na raiz deste repositório backend, PowerShell:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/nutri_kids"
$env:DB_USERNAME = "nutri_kids_app"
# Solicita senha sem gravá-la no histórico do terminal:
$dbSecret = Read-Host "Senha do PostgreSQL" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $dbSecret).Password
$env:BOOTSTRAP_EMAIL = Read-Host "E-mail do administrador inicial"
$adminSecret = Read-Host "Senha inicial (12 caracteres ou mais, até 72 bytes UTF-8)" -AsSecureString
$env:BOOTSTRAP_PASSWORD = [System.Net.NetworkCredential]::new("", $adminSecret).Password
.\scripts\iniciar.ps1
```

O bootstrap só cria clínica e administrador quando não há usuários. Após criar o administrador, remova BOOTSTRAP_PASSWORD do ambiente e reinicie sem ela:

```powershell
Remove-Item Env:BOOTSTRAP_PASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:BOOTSTRAP_EMAIL -ErrorAction SilentlyContinue
```

A API inicia em http://localhost:8080. O frontend React é um projeto separado; durante desenvolvimento seu Vite deve encaminhar `/api` a http://127.0.0.1:8080. Não há frontend estático embutido neste repositório. Use cookies e CSRF na mesma origem, por proxy reverso em produção.

`.env.example` é referência; **Spring não importa `.env` automaticamente**. Use variáveis do PowerShell ou da IDE.

## Primeiro fluxo

1. Faça login com o administrador inicial.
2. Em Clínica e equipe, altere o nome e cadastre nutricionista e responsável.
3. Cadastre paciente e abra a ficha. Vincule o responsável à criança.
4. Registre anamnese, evolução e medidas. Marque explicitamente o que pode ser compartilhado.
5. Crie uma consulta, conclua/cancele e exporte registros em PDF.
6. Em outra sessão, entre como responsável. Confira que apenas crianças vinculadas e conteúdo compartilhado aparecem. Registre diário/mensagem e anexe um arquivo sintético.

## Testar

```powershell
.\scripts\verificar.ps1
```

Os testes backend padrão usam H2 em memória e dados sintéticos, sem Docker ou banco instalado. Não comprovam migrações/constraints PostgreSQL. Há roteiro específico e perfil PostgreSQL em docs/validacao.md.

## API principal

| Rota | Operações |
|---|---|
| `/api/auth/csrf`, `/login`, `/me`, `/logout` | CSRF, login, sessão atual e encerramento (demais rotas sob `/api/auth`) |
| `/api/clinica` | GET/PUT |
| `/api/equipe` | GET/POST |
| `/api/pacientes` | GET/POST |
| `/api/pacientes/{id}` | GET/PUT |
| `/api/pacientes/{id}/responsaveis` | POST vínculo administrativo |
| `/api/pacientes/{id}/registros` | GET/POST por tipo |
| `/api/pacientes/{id}/medidas` | GET/POST |
| `/api/pacientes/{id}/anexos` | GET/POST multipart |
| `/api/pacientes/{id}/anexos/{arquivoId}` | GET autenticado |
| `/api/pacientes/{id}/relatorio.pdf` | GET PDF |
| `/api/agenda`, `/api/agenda/{id}` | GET/POST e PATCH status |

Todo POST/PUT/PATCH e logout requer `X-XSRF-TOKEN`, obtido em `/api/auth/csrf`, além do cookie da sessão. Nenhuma chave ou credencial deve ser versionada. Não há dados reais de pacientes no repositório.

## Repositórios

Backend: https://github.com/Wendersonjose/NutriKids. Este repositório contém pom.xml e src/ na raiz, documentação, scripts e CI do backend. O frontend React é mantido separadamente.

Nenhum projeto anterior de nutrição foi alterado. Consulte o histórico Git e o resultado final da publicação informado na entrega.
