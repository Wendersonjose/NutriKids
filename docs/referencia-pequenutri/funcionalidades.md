# Matriz funcional

As descrições abaixo são sínteses das evidências, não transcrições. Recursos anunciados não foram testados com conta de cliente. P1/P2/P3 referem-se ao nosso projeto.

| Funcionalidade | Descrição | Fonte | Evidência | Prioridade | Proposta de implementação |
|---|---|---|---|---|---|
| Cadastro e histórico | Paciente e atendimentos | S1/S4 | Declarada | P1 | Cadastro isolado por clínica, histórico cronológico |
| Agenda | Organização de consultas | S1 e captura agenda | Declarada e imagem | P1 | Intervalos com exclusão de sobreposição no banco |
| Prontuário | Registro por consulta | S1 | Declarada | P1 | Registros com autoria; vincular consulta em etapa posterior |
| Anamnese | Questionário de entrada | S1/S4 | Declarada | P1 | Texto clínico inicial; formulário versionado no backlog |
| Antropometria | Crescimento pediátrico | S1/S5 | Declarada | P1/P2 | Medidas primeiro; OMS somente após validação |
| Plano alimentar | Orientação acessível à família | S4/S5 | Declarada | P2 | Orientação manual; receitas e nutrientes depois |
| PDF | Compartilhamento de plano | S1 | Declarada | P1 | Relatório de registros, com filtro de autorização |
| Diário | Refeições e aceitação | S4 | Declarada | P2 | Registros familiares datados; campos estruturados depois |
| Aplicativo familiar | Acesso vinculado ao paciente | S4/S6 | Declarada | P2 | Portal web responsivo com autorização por vínculo |
| Comunicação | Troca entre família e clínica | S4 | Declarada | P2 | Mensagens textuais assíncronas inicialmente |
| Anexos | Exames e fotos | S4 | Declarada | P2 | Download autenticado, validação de tipo e tamanho |
| Terapia | Evolução de aproximação alimentar | S1 | Demonstração promocional | P2 | Registro do profissional; sem protocolo clínico inventado |
| Jogos | Atividades supervisionadas | S1 e imagens S5 | Declarada e imagens | P3 | Conteúdo original, revisado por nutricionista |
| Financeiro | Gestão do consultório | S1 | Declarada | P3 | Razão de lançamentos, não confundir com assinatura SaaS |
| Assinaturas | Planos comerciais | S1 | Página comercial | P3 | Entitlements e pagamento com webhooks idempotentes |
| Notificações | Atualizações da família | S4 | Declarada | P2 | Caixa de avisos e push após escolha de fornecedor |

Os campos completos de prontuário, anamnese, avaliação sensorial e prescrição não foram observados. Os campos do nosso cadastro são decisões próprias e não devem ser apresentados como reprodução do Pequenutri.
