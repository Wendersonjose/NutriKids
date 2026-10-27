# APIs incrementais

Sessão e CSRF mantidos: obter `/api/auth/csrf`, autenticar e enviar `X-XSRF-TOKEN` nas escritas. Perfis profissionais são ADMIN/NUTRICIONISTA; financeiro e assinatura exigem ADMIN. Acesso por clínica e vínculo de paciente é conferido no servidor, sem confiar em clínica enviada no corpo.

| Método | Caminho | Uso |
|---|---|---|
| GET | `/api/pacientes/{id}/crescimento?edema=false` | Histórico Z/percentis/alertas autorizado |
| GET/POST | `/api/alimentos` | Catálogo da clínica / cadastro profissional |
| POST | `/api/alimentos/calcular` | `itens: [{alimentoId, quantidade, medida?}]`; quantidade em g se sem medida |
| POST | `/api/pacientes/{id}/plano-calculado` | `titulo`, `compartilhado`, `refeicoes:[{nome,itens:[...]}]`; cria versão no histórico |
| GET | `/api/pacientes/{id}/relatorio.pdf` | PDF existente inclui os planos calculados e respeita compartilhamento |
| GET/POST | `/api/pacientes/{id}/jogos/memoria` | Lista/retoma e inicia partida |
| POST | `/api/pacientes/{id}/jogos/memoria/{partida}/jogadas` | `primeira`, `segunda`, índices 0–11; servidor calcula progresso |
| GET/POST | `/api/financeiro` | Lista por `inicio`/`fim` de competência / cria receita ou despesa |
| POST | `/api/financeiro/{id}/liquidar` | `pagamento` e `referencia`; repetição idêntica não duplica |
| GET | `/api/financeiro/resumo?inicio=...&fim=...` | Valores liquidados/a pagar/a receber no recorte de competência |
| GET | `/api/financeiro/vencimentos?inicio=...&fim=...` | Filtro por vencimento |
| GET | `/api/financeiro/fluxo-caixa?inicio=...&fim=...` | Movimento líquido por dia de pagamento; sem saldo inicial presumido |
| GET | `/api/assinatura` e `/api/assinatura/faturas` | Estado e faturas da clínica; modelo sandbox parcial |
| POST | `/api/webhooks/asaas-sandbox` | Token próprio `asaas-access-token`; única nova rota sem sessão/CSRF |
| GET/POST | `/api/pacientes/{id}/privacidade/solicitacoes` | Pedido ACESSO/CORRECAO/EXCLUSAO/INFORMACAO; responsável vê somente seus pedidos |
| POST | `/api/pacientes/{id}/privacidade/solicitacoes/{pedido}/resposta` | ADMIN, status CONCLUIDA/INDEFERIDA e resposta documentada |

Novos arquivos principais: `OmsService/OmsController`, `Alimento*/Composicao/PlanoCalculadoController`, `Lancamento*/FinanceiroController`, `Partida*/JogoController`, pacote `assinatura`, `LimiteApi` e `auditoria/PrivacidadeController`. Frontend: `Modules.tsx` e `Nutricao.tsx` integrados ao `App.tsx` preservado.

Migrations adicionadas: **V3__alimentos_financeiro.sql**, **V4__memoria_alimentar.sql**, **V5__assinaturas_sandbox.sql**, **V6__direitos_titular.sql**. Não editar migrations já aplicadas.
