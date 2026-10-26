# Estado da entrega — 26/10/2026

## Implementado e coberto por testes

- API Java 17 / Spring Boot 3.5.16, autenticação por sessão e logout, CSRF e BCrypt.
- Perfis ADMIN, NUTRICIONISTA e RESPONSAVEL; limite local de tentativas de login.
- Clínica inicial, alteração do nome, criação de usuários/nutricionistas/responsáveis e vínculo à criança.
- Pacientes: criação, listagem por autorização, ficha e edição.
- Agenda: criação, listagem, conclusão/cancelamento, validação de profissional/clínica e intervalo.
- Histórico de registros com autoria, data e compartilhamento explícito.
- Anamnese, evolução, plano alimentar manual, diário, terapia, metas e mensagens como registros textuais tipados.
- Antropometria: peso, altura/comprimento, posição e data; validação de medida anterior ao nascimento.
- Portal familiar web: somente crianças vinculadas, registros compartilhados, diário/mensagens e anexos.
- Upload PDF/PNG/JPEG até 5 MB e download autenticado; compartilhamento restrito.
- Relatório PDF de registros autorizados, inclusive quando há várias páginas.
- Frontend React responsivo conectado à API, erros e carregamento; dashboard com contagens reais.
- Auditoria de criação, leitura de recursos e exportação, sem conteúdo clínico no log.

## Parcial

Anamnese e planos são textuais: não há formulário clínico estruturado, composição nutricional ou modelos validados. Mensagens não são chat em tempo real. Metas e terapia são anotações, sem progressão automática ou recompensas. O relatório exporta textos; não incorpora imagens, curvas ou assinatura digital. Pacientes não têm campos completos de endereço, contatos e documentos nesta versão. O responsável é criado manualmente, sem convite por e-mail. Auditoria não é inviolável nem cobre todos os eventos possíveis.

## Preparado, ainda sem validação PostgreSQL

Migrações Flyway V1/V2, índices, chaves compostas por clínica e exclusion constraints de agenda. A suite PostgreSQL é opt-in e foi pulada por falta de banco. Configuração padrão do produto continua PostgreSQL; H2 é exclusivo dos testes/verificação sintética.

## Pendente

- Curvas OMS e escores validados, correção por prematuridade e revisão profissional.
- Biblioteca licenciada de alimentos/nutrientes, refeições, porções e substituições.
- Formulários de anamnese e terapia estruturados, histórico por consulta.
- Financeiro, pagamentos, assinatura SaaS, webhooks e painel comercial.
- Jogos, gamificação, recompensas, notificações, PWA e aplicativos nativos.
- Troca/recuperação de senha, convites, revogação de conta e MFA.
- Consentimento versionado, política de retenção executável, exclusão/portabilidade e revisão de LGPD.
- Paginação, limites por clínica, armazenamento externo, antimalware e sessão compartilhada.
- Deploy de produção e revisão independente de segurança.

O núcleo é funcional; isso não significa que todos os 33 itens da missão estejam concluídos. Não usar dados clínicos reais antes da validação de banco, revisão de segurança e revisão clínica.
