# Validação técnica — 26/10/2026

Base preservada: 6c9e719. Branch: feat/modulos-validacao. PR: https://github.com/Wendersonjose/NutriKids/pull/1. Sem Docker, dados exclusivamente sintéticos.

## Execuções confirmadas antes dos overrides Java de segurança

CI confirmado: https://github.com/Wendersonjose/NutriKids/actions/runs/40423613333 (commit 9da31c8).

| Verificação | Resultado observado |
|---|---|
| Maven verify local | 48 listados: 47 executados, zero falhas/erros, 1 teste PostgreSQL pulado na execução anterior à criação da classe PostgreSQL herdada |
| Maven verify CI | 74 listados: 47 executados, zero falhas/erros, 27 opt-in PostgreSQL pulados nesse job |
| PostgreSQL real CI | 27 executados: 26 fluxos HTTP/autorizações herdados + 1 teste de migration/constraint, zero falhas/erros/pulados |
| PostgreSQL/Flyway | PostgreSQL 16.15; 6 migrations aplicadas; Hibernate validou esquema |
| Backup/restore CI | pg_dump custom + pg_restore --exit-on-error em banco separado; registro sintético e histórico Flyway conferidos |
| Frontend Vitest | 12 testes passaram, zero falhas |
| Frontend TypeScript/Vite | Build de produção passou |
| npm audit | 3 achados iniciais corrigidos; nova execução reportou 0 vulnerabilidades conhecidas |

Os mesmos 26 fluxos são intencionalmente executados com H2 e PostgreSQL, não são 52 cenários distintos. Os testes de PostgreSQL são pulados no job geral e executados separadamente no job postgres; isso não representa ausência de teste de banco.

## Cobertura significativa

- Login/logout/CSRF, validação de senha, sessão e privacidade HTTP.
- Isolamento entre clínicas; família sem vínculo; observações/registros/anexos privados; perfil nutricionista sem administração financeira/equipe administrativa.
- Agenda/transições, referências de paciente/profissional e constraint PostgreSQL contra sobreposição.
- Fluxo paciente/registro/medida/PDF; upload forjado recusado; anexos autenticados.
- OMS: oito testes, incluindo dois resultados de referência oficiais, limites etários/posição, edema, extremo, sexo desconhecido, NaN e normal acumulada.
- Composição: cálculo decimal, totalização, ausência distinta de zero, quantidade inválida e rendimento medido da preparação.
- Catálogo/medida caseira, plano calculado persistido e tentativa de cálculo com alimento de outra clínica.
- Financeiro: precisão de centavos, baixa idêntica sem duplicação, resumo e tentativa de baixa por outra clínica/perfil indevido.
- Jogo: tabuleiro não exposto, partida completa e conquista persistida, bloqueio de paciente não autorizado.
- Webhooks: segredo inválido, repetição, fonte canônica consultada em vez de confiar no corpo, cliente divergente, sandbox sem credenciais.
- Privacidade: paciente/vínculo, pedido e resposta administrativa; webhook sem sessão ainda exige segredo próprio.
- Frontend: login, fluxos existentes, CSRF, opção explícita de edema, duas cartas enviadas à API e bloqueio de plano vazio.

## Falhas encontradas e corrigidas

1. Carga inicial OMS sem cópia dos arquivos no diretório correto: corrigido e recompilado.
2. Anotação type-use em tipo interno qualificado no plano: CI encontrou erro de compilação; import explícito de Item corrigiu.
3. Tipagem de teste Testing Library com opção inexistente: corrigido; TypeScript passou.
4. npm audit encontrou problemas nas ferramentas de teste: Vitest atualizado para 5.0.5, lock revisado e 12 testes passaram.
5. Git push local sem credencial: publicação efetuada pelo conector GitHub em branch nova, sem alterações em main.
6. Ambiente local não permite usuário sem privilégios para PostgreSQL: validação transferida ao runner nativo do CI, com resultado positivo.

## Comandos reproduzíveis

```powershell
mvn verify
Set-Location frontend
npm ci
npm test
npm run build
npm audit
```

Para banco PostgreSQL **separado e descartável**, com credenciais de teste já configuradas:

```powershell
$env:TEST_DB_URL = 'jdbc:postgresql://localhost:5432/nutri_kids_test'
$env:TEST_DB_USERNAME = 'nutri_kids_test'
# Defina TEST_DB_PASSWORD pelo mecanismo seguro do seu ambiente; não use produção.
mvn '-Dtest=PostgresMigrationTest,PostgresFluxoClinicoTest' test
```

CI nativo e restauração: .github/workflows/ci.yml e scripts/testar-restauracao-ci.sh. O script é específico do banco descartável do CI e não deve ser apontado para produção.

## Limites

Sem sandbox Asaas credenciado: testes de webhook usam respostas canônicas sintéticas via mock; nenhuma comunicação/cobrança real foi executada. Sem binários WHO Anthro/AnthroPlus ou pacote R executados: evidência clínica vem de tabelas/testes oficiais publicados. Sem varredura completa de todas as dependências Java, teste de carga, teste concorrente completo, revisão independente de segurança ou QA visual em navegador real. Não liberar uso clínico com base somente nestes testes.

## Confirmação final dos overrides de segurança

CI final: https://github.com/Wendersonjose/NutriKids/actions/runs/40423842740 — commit `11434cc6aa0b630e21ea27948082abeca5a9dad2`.

Ambos os jobs concluídos com sucesso: Maven verify, 47 testes gerais executados (27 opt-in pulados nesse job), 27 testes PostgreSQL executados no job dedicado, seis migrations, backup/restore com conferência de conteúdo, 12 testes frontend e build TypeScript/Vite. Isso inclui os overrides Tomcat 10.1.61 e Jackson 2.21.8. Sem falhas/erros. Localmente esses novos artefatos não puderam ser baixados por bloqueio de DNS; a validação final ocorreu no CI.

Varredura local de padrões de tokens GitHub, chaves AWS e cabeçalhos de chave privada nos arquivos versionados: nenhum achado de alto sinal. Não é uma análise exaustiva de segredos.
