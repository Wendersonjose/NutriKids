# Investigação técnica pública

Sem acesso autenticado, enumeração de endpoints, coleta de dados de pacientes ou testes de vulnerabilidade.

| Observação | Nível | Evidência | O que não permite concluir |
|---|---|---|---|
| Aplicação web utiliza inicialização Flutter | Confirmado no artefato público | HTML GET de https://app.pequenutri.com.br/login contém referências Flutter e bootstrap | Versão do SDK, arquitetura móvel ou servidor |
| Site contém artefatos `_astro/` e atributos `data-astro-*` | Indício forte de Astro | HTML GET de https://pequenutri.com.br/ | Todos os frameworks do ecossistema |
| Aplicativos iOS e Android publicados | Confirmado publicamente | S4/S5; identificador Android com.pequenutri.app | Natividade, linguagem ou compartilhamento de código móvel |
| Domínio público `api.pequenutri.com.br` | Confirmado | Política vinculada nas lojas | Rotas clínicas, framework backend ou banco |
| Stripe, Firebase, Meta e Sentry | Declarado pelo fornecedor | [Política](https://api.pequenutri.com.br/privacy-policy), atualizada 07/10/2026 | SDKs ativos em toda tela ou validação independente de segurança |
| HTTPS, hash de senha, isolamento e auditoria | Declaração do fornecedor | Mesma política pública | Algoritmo, cobertura, pentest, certificação ou ausência de falhas |
| Banco, ORM, framework API e hospedagem | Não identificados | Não houve evidência suficiente | Não atribuir PostgreSQL, Supabase, Java, Node ou outra tecnologia por palpite |
| Autenticação | Login e convite descritos | S1/S4/S5 | Uso de JWT, cookies, OAuth, MFA ou renovação de token |
| Repositório oficial | Não localizado | Busca pública GitHub `pequenutri` sem resultado | Não demonstra inexistência de repositório privado |

A nova plataforma usa a stack solicitada pelo usuário; essa escolha não resulta de uma alegação sobre a stack do concorrente.
