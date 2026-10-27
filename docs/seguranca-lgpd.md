# Segurança, privacidade e responsabilidades — 26/10/2026

Controles parciais implementados; sem certificação ASVS, parecer jurídico ou liberação para dados reais.

## Controles verificáveis

- Sessão expira após 30 minutos de inatividade; login invalida sessão anterior. Cookie HttpOnly/SameSite strict; produção exige HTTPS e COOKIE_SECURE=true. Não houve implantação de produção.
- BCrypt custo 12; limite de login por IP real. Limite API de 240 requisições/minuto por usuário ou IP, com no máximo 10 mil chaves em memória. Não é distribuído nem substitui limites por clínica. Não confiar em X-Forwarded-For arbitrário.
- CSRF nas escritas com sessão. Exceção exata para /api/webhooks/asaas-sandbox, com token próprio de pelo menos 32 caracteres, comparação em tempo constante e falha fechada sem configuração.
- Clínica no filtro de repositório; paciente/vínculo no serviço; família somente vê registros e anexos compartilhados. Financeiro e assinatura exigem ADMIN. Catálogo/cálculos exigem profissional.
- Valores financeiros BigDecimal/numeric(14,2), centavos validados, transação e lock pessimista/versionamento. Baixa idêntica não duplica; alterações posteriores conflitam. Estorno e baixa parcial pendentes.
- Jogo mantém tabuleiro e progresso no servidor; não aceita pontuação do cliente. Lock por partida serializa jogadas. Não há promessa de avaliação clínica pelo jogo.
- Upload PDF/PNG/JPEG até 5 MB com assinatura de bytes; conteúdo no banco, download autenticado/attachment/no-store. Magic bytes não removem malware, metadados ou scripts em PDF. Antivírus/quarentena pendentes.
- Auditoria de ações/autor/clínica/recurso, sem conteúdo clínico. Log não é inviolável e precisa política de retenção.
- Solicitações de ACESSO/CORRECAO/EXCLUSAO/INFORMACAO por paciente e resposta de ADMIN auditada. Família lista somente seus pedidos e precisa de vínculo. Registrar decisão não executa automaticamente exclusão, correção ou exportação integral.
- Asaas: URL fixa de sandbox, sem criação de cobranças ou captura de cartão. Webhook reconsulta situação canônica e valida assinatura/cliente/valor, deduplica ID e usa transação. Onboarding de assinantes ainda pendente.

## Matriz de acesso

| Recurso | ADMIN | NUTRICIONISTA | RESPONSAVEL |
|---|---|---|---|
| Pacientes | Própria clínica | Própria clínica | Somente vínculo |
| Registros e anexos | Clínica/paciente | Clínica/paciente | Vínculo e compartilhamento |
| Catálogo e cálculos | Sim | Sim | Não |
| Jogos | Paciente da clínica | Paciente da clínica | Paciente vinculado |
| Financeiro/assinatura | Própria clínica | Não | Não |
| Pedidos de titular | Criar/listar/responder | Criar/listar | Criar/listar próprios |

## Governança a aprovar

Confirmar controlador, operador e encarregado por operação. A clínica tende a controlar finalidades clínicas e o SaaS tende a operar por suas instruções; cobrança comercial própria pode ter papéis diferentes. Firmar contrato, inventariar suboperadores e avaliar transferências internacionais.

Separar finalidades de atendimento, comunicação, jogos, financeiro e cobrança SaaS. Dados de saúde exigem hipóteses específicas para dados sensíveis; interesse legítimo não é autorização genérica para prontuário. Documentar necessidade e base legal por finalidade. Consentimento, quando aplicável, precisa versão/finalidade/data/representante e revogação; esse registro versionado ainda não foi implementado.

Melhor interesse da criança: minimizar dados, confirmar identidade e representação do adulto antes do vínculo e avaliar cada compartilhamento. Não há login infantil autônomo. Conquista do jogo representa associação de nomes; não premia ingestão, restrição alimentar ou perda de peso.

Definir matriz de retenção para prontuário, anexos, auditoria, financeiro, suporte e backups, considerando obrigações aplicáveis. Nenhum prazo arbitrário ou job de eliminação foi criado. Exclusão de conta não equivale à eliminação de prontuário. Pedidos devem ter identidade/representação conferidas, análise de conservação, execução controlada e evidência. O módulo atual implementa recebimento e decisão; execução/portabilidade integral seguem pendentes.

## Backups e incidentes

CI prepara PostgreSQL nativo e restauração em banco separado somente com dados sintéticos; verificar resultado efetivo em docs/validacao.md. Para produção: backup cifrado, chave segregada, privilégio mínimo, retenção, restauração periódica com conferência de conteúdo e RPO/RTO definidos. CI não comprova a estratégia do futuro ambiente hospedado.

Incidente: detectar e conter, preservar evidências, revogar acessos afetados, dimensionar dados/impacto e informar imediatamente o controlador pelo canal contratual. Controlador avalia comunicação à ANPD/titulares conforme regulamento vigente, registra cronologia e correções. Comunicação oficial via SEI!ANPD; enquadramento e prazo devem ser revisados no caso concreto. Não há notificação legal automática.

## Fontes oficiais consultadas em 26/10/2026

- https://asvs.owasp.org/ — ASVS 5.0.0.
- https://owasp.org/API-Security/editions/2023/en/0x00-header/
- https://www.gov.br/anpd/pt-br/assuntos/noticias/anpd-divulga-enunciado-sobre-o-tratamento-de-dados-pessoais-de-criancas-e-adolescentes
- https://www.gov.br/anpd/pt-br/acesso-a-informacao/institucional/atos-normativos/regulamentacoes_anpd/
- https://www.gov.br/anpd/pt-br/canais_atendimento/agente-de-tratamento/comunicado-de-incidente-de-seguranca-cis
- https://docs.asaas.com/docs/sobre-os-webhooks
- https://docs.asaas.com/docs/receba-eventos-do-asaas-no-seu-endpoint-de-webhook
- https://docs.asaas.com/docs/sandbox

## Barreiras de liberação

Troca/recuperação de senha e revogação, MFA administrativo, antivírus, rotação de segredos, análise atualizada de dependências Java, governança/consentimento, revisão clínica e teste independente de segurança/concorrência. Auditoria npm detectou e corrigiu vulnerabilidades de ferramentas de teste; isso não comprova ausência de vulnerabilidades no sistema inteiro.
