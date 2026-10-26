# Validação executada — 26/10/2026

| Verificação | Resultado |
|---|---|
| Java 17 / Maven verify / Spring Boot 3.5.16 | BUILD SUCCESS; JAR executável gerado |
| Backend JUnit 5 / Mockito / MockMvc | 21 testes passaram; zero falhas/erros |
| PostgreSQL específico | 1 teste pulado; banco não disponível |
| Frontend Vitest | 9 testes passaram; zero falhas |
| TypeScript + Vite production build | Sucesso |
| HTTP real via Vite + JAR Spring Boot | 8 verificações passaram: HTML, login/CSRF, paciente, plano, medidas, dashboard, PDF e logout; H2 sintético |
| Teste visual em navegador desktop/mobile | Não concluído: navegador remoto não alcança localhost do ambiente |

## Cobertura significativa

Backend: login inválido, exigência de autenticação/CSRF, logout, isolamento de clínicas, autorização por vínculo, proteção de observações clínicas, filtro de compartilhamento, proibição de escrita clínica familiar, criação de registros/medidas/PDF, data anterior ao nascimento, profissional de outra clínica, transição de consulta, anexos privados, tipo forjado, envio familiar, ausência de hash em JSON, privilégio de administrador, no-store, limite de tentativas e uso do clinic_id da sessão no repositório.

Frontend: obtenção de CSRF, cookie de sessão, erros de validação, evento de expiração, multipart sem Content-Type incorreto, logout 204, login renderizado, navegação familiar sem opções administrativas, erro na listagem e retorno à entrada após expiração.

## Limites

H2 cria o esquema de teste com Hibernate; não roda migrações Flyway nem reproduz GiST, btree_gist, timezone, isolamento/concor­rência e permissões reais do PostgreSQL. Não declarar PostgreSQL validado com base nesses testes. Responsividade foi implementada por CSS, sem inspeção visual final nos dois tamanhos de tela. CI remoto tem resultado independente, a verificar no GitHub.

## Executar integração PostgreSQL sem Docker

Crie banco **descartável separado**, nunca aponte para produção:

```powershell
$env:TEST_DB_URL = "jdbc:postgresql://localhost:5432/nutri_kids_test"
$env:TEST_DB_USERNAME = "nutri_kids_test_app"
$testDbSecret = Read-Host "Senha do banco de teste" -AsSecureString
$env:TEST_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $testDbSecret).Password
mvn verify
```

O perfil postgres-test executa Flyway e Hibernate validate no banco informado. A migration fica aplicada; o método de teste usa transação revertida. O teste confere aplicação do esquema e rejeição de horário sobreposto. Complementar com o roteiro de concorrência, reinicialização, privilégios e backup em postgresql-windows.md.
