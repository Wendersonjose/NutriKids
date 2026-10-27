# Estado da entrega — 26/10/2026

Entrega incremental no projeto existente, PR #1. Não está pronto para dados clínicos reais.

## Núcleo preservado

Login/logout/CSRF/BCrypt; clínica/equipe; pacientes/responsáveis e vínculo; agenda; prontuário/anamnese/diário/mensagens/metas/terapia textuais; antropometria; anexos privados; PDF; dashboard e portal familiar responsivo.

## Novos fluxos implementados

- OMS 2006: quatro indicadores para menores de 60 meses, Z, percentis, ajuste de posição e avisos; gráfico longitudinal em Z. Oito testes, dois casos oficiais publicados. Homologação clínica ainda pendente.
- Catálogo alimentar por clínica com fonte/versão/permissão, composição por 100 g, medidas caseiras e cálculo por porção/refeição/dia. Nenhuma base TBCA/TACO importada.
- Plano calculado editável na tela: salvar preserva uma nova versão textual com fontes no histórico e no PDF existente. Não há edição estruturada de versões históricas.
- Jogo da memória original com 12 cartas, progresso, retomada, conquista e acompanhamento persistido, sem punição por tentativas.
- Financeiro ADMIN: receitas/despesas, a pagar/receber, liquidação/conciliacão básica, filtros por competência/vencimento, resumo e fluxo diário por data do pagamento. Precisão decimal e transações.
- Infraestrutura parcial de assinaturas: modelos/faturas, consulta administrativa e webhook Asaas autenticado/idempotente com reconsulta exclusiva de sandbox. Nenhuma criação de cobrança habilitada.
- Limite geral API por identidade/IP; solicitações de direitos do titular com decisão administrativa auditada.
- Migrations V3–V6. Backend e frontend versionados juntos, preservando backend Maven na raiz e frontend em subprojeto.
- CI nativo sem Docker validou PostgreSQL 16.15, seis migrations, 27 testes de integração/autorização e restauração em banco separado. Consulte validacao.md para versão exata testada.

## Pendente

OMS 2007/5–19 anos e comparação executada com AnthroPlus; vetores ampliados/revisão clínica; edema por avaliação; prematuridade; catálogo licenciado e receitas persistentes; substituições com equivalência revisada; anamnese/terapia estruturadas; demais jogos/níveis/desafios; financeiro avançado/contas e categorias normalizadas; assinaturas completas/trial/cancelamento/inadimplência; consentimento versionado, retenção executável, portabilidade/exclusão; recuperação/revogação de senha/MFA/antivírus; paginação, limites distribuídos, PWA e revisão independente.

Referências, APIs e critérios de aceite: validacao-clinica.md, seguranca-lgpd.md, dependencias.md, apis-incrementais.md e auditoria-e-backlog.md.
