# Auditoria e backlog — 26/10/2026

Base auditada: `6c9e719143889890f635bcd6f1088e1167eb7a36` do repositório existente. Não se recriou a aplicação; migrations V1/V2 preservadas. Pacotes financeiro, assinatura, gamificação e planoalimentar eram somente marcadores. O frontend existente separado foi incorporado de forma aditiva em `frontend/`, mantendo Maven e backend na raiz.

## Núcleo preservado

Autenticação com sessão/BCrypt 12/CSRF, clínica/equipe, pacientes/vínculos, consultas, registros compartilhados, antropometria, anexos autenticados, PDF e auditoria. Sessão de 30 minutos, cookie HttpOnly/SameSite strict; Secure depende da variável de produção. Testes iniciais: 22 listados, zero falhas, um teste PostgreSQL não executado.

## Incrementos

| Área | Estado desta entrega | Limite |
|---|---|---|
| OMS 2006 | API com 4 indicadores, alertas e frontend Z longitudinal | <60 meses, sem homologação clínica |
| Alimentos | Cadastro com proveniência, medidas por alimento e cálculo decimal | Sem importação licenciada, padronização e revisão de dados |
| Planos | Composição por refeição, total diário e versão textual no histórico/PDF | Sem editor histórico estruturado/receitas persistentes |
| Jogo | Memória original de 12 cartas, progresso e conquista persistidos, retomada | Um jogo; sem níveis ou desafios personalizados |
| Financeiro | Receitas/despesas, a pagar/receber, liquidação, resumo, vencimentos e fluxo por dia | Categorias/contas são rótulos; sem cadastro normalizado, parcelas, estorno ou pagamento parcial |
| Assinaturas | Modelos/faturas, consulta administrativa, webhook autenticado/idempotente e cliente sandbox | Sem onboarding, criação/cancelamento de recorrência, trial ou restrição comercial |
| Privacidade | Solicitações por paciente e resposta administrativa auditada | Sem executor automático de exclusão/exportação completa |
| Segurança | Limite API por identidade/IP e ampliação de testes negativos | Limites em memória, sem compartilhamento entre instâncias |
| Banco | V3–V6 e job nativo PostgreSQL/backup/restore no CI | Consultar resultado de execução no relatório |

## Prioridades restantes

| Prioridade | Trabalho | Critério de aceite |
|---|---|---|
| P0 | Revisão clínica OMS 2006 e alimentar | Vetores independentes, tolerâncias e aprovação do responsável técnico |
| P0 | OMS 2007/AnthroPlus | Faixas específicas, tabelas licenciadas e testes completos de fronteira 5/10/19 anos |
| P0 | Segurança operacional | TLS/cookie Secure, revogação, troca/recuperação de senha, MFA, antivírus e teste independente |
| P0 | Governança LGPD | Inventário, bases legais por finalidade, melhor interesse, retenção, contrato controlador/operador e consentimento quando aplicável |
| P0 | Homologação PostgreSQL | Migrations e testes reais, restauração com dados sintéticos, concorrência e constraints |
| P1 | Assinaturas completas | Conta sandbox, onboarding seguro, planos, trial, criação sem duplicação em timeout, cancelamento, inadimplência e reconciliação |
| P1 | Composição e receitas | Licença TBCA/TACO, revisão de unidades, importador rastreável, editor e rendimento persistido |
| P1 | Financeiro avançado | Contas/categorias normalizadas, parcelas, pagamento parcial, estorno e conciliação por extrato |
| P1 | Direitos dos titulares | Exportação completa, execução controlada de correções/exclusão, retenção e comprovação de representação |
| P2 | Expansão de jogos | Mais jogos, níveis e atividades personalizadas com revisão pedagógica |
| P2 | Escala/UX | Paginação, limites por clínica, cache seguro, sessão compartilhada, PWA e testes visuais desktop/mobile |

Não há dependência de Docker. Nenhum recurso foi publicado como pronto para uso clínico real.
