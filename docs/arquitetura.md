# Arquitetura

Monólito modular Java 17 / Spring Boot 3.5.16, API REST e React TypeScript/Vite. Pacotes seguem domínio. Módulos futuros possuem apenas package-info; isso não representa funcionalidade implementada. Prontuário concentra registros textuais de anamnese, plano, diário, terapia, mensagens e metas neste incremento; a separação em agregados estruturados é uma evolução planejada.

A sessão autenticada define usuário, clínica e perfil. O cliente não escolhe a clínica em requests de dados. Todas as consultas de recurso usam clinic_id da identidade autenticada; vínculos compostos no PostgreSQL impedem associações entre clínicas. Responsáveis acessam somente pacientes vinculados e registros explicitamente compartilhados. Equipe ADMIN/NUTRICIONISTA tem acesso clínico na própria clínica; ADMIN gere contas e vínculos. Não existe perfil de secretária nesta versão.

Cookies HttpOnly/SameSite Strict, CSRF por header, BCrypt custo 12 e limite local de tentativas. Senhas não retornam no JSON. No navegador, usar o proxy Vite `/api`; não guardar credenciais em localStorage. Em produção, servir frontend/API na mesma origem, usar HTTPS, COOKIE_SECURE=true e proxy reverso. Backend escuta localhost por padrão. Sessões e limitador são em memória; múltiplas instâncias exigem sessão compartilhada e limite distribuído.

Agenda representa intervalos [início,fim). PostgreSQL aplica exclusion constraints para profissional e paciente com status AGENDADA. Isso protege inclusive requisições concorrentes. Hibernate usa validate; Flyway aplica mudanças. H2 de teste não comprova exclusion constraints PostgreSQL.

Registros clínicos são acrescentados, sem endpoint de alteração/exclusão. Auditoria registra metadados de leitura/exportação e criação, sem copiar conteúdo de saúde. Auditoria não é armazenamento inviolável e ainda não cobre todos os eventos de autenticação ou mudanças administrativas futuras.

Uploads são armazenados no banco como bytea, até 5 MB, com título e compartilhamento. Download exige autorização por paciente e arquivo, como attachment e nosniff. Assinatura inicial de PDF/PNG/JPEG é verificada; não equivale a antimalware nem validação completa. Não há diretório público de anexos. Backups precisam incluir binários e ser criptografados externamente.

Não existem decisões clínicas automáticas. Medidas são registros; planos são orientações manuais do profissional. Não há afirmação de conformidade LGPD integral ou prontuário certificado.

## Auditoria arquitetural de 26/10/2026

Base examinada: main, commit `6c9e719143889890f635bcd6f1088e1167eb7a36`. Este repositório contém somente o backend; React/TypeScript não estão nesta árvore e não foram alterados.

Controllers de agenda, pacientes, equipe/clínica, registros, medidas, anexos, relatórios e dashboard agora delegam a services de domínio. Transações de escrita ficam nos métodos públicos dos services, chamados pelo proxy Spring. DTOs de entrada mantêm Bean Validation; DTOs de saída usam listas explícitas de campos. Nenhum controller retorna entidade JPA. Login continua no controller porque manipula diretamente sessão e SecurityContext; não se alterou seu mecanismo nesta refatoração.

Isolamento: clínica sempre vem de Identidade; AcessoPaciente centraliza acesso por clínica e vínculo. Administrador e nutricionista podem atender qualquer paciente da própria clínica; não existe vínculo exclusivo paciente→nutricionista. Responsável só lê conteúdo compartilhado dos pacientes vinculados e só cria diário/mensagem/anexo nesses pacientes. Relatório PDF usa as mesmas regras; identificadores de outra clínica respondem 404. DTOs não retornam clinicaId/segredos desnecessários; senha e bytes continuam protegidos também nas entidades como defesa adicional.

Agenda: comparação de Duration integral aceita até 8h exatas e rejeita 8h + 1ms (o código anterior truncava para horas e aceitava 8h59). V3 adiciona CHECK no PostgreSQL, preservando V1/V2. Exclusion constraints de paciente/profissional permanecem a autoridade contra corrida de agendamento. @Version mantém proteção contra transições simultâneas; saveAndFlush permite responder 409 antes de construir a resposta. Isso não implementa precondição If-Match para edições sequenciais feitas a partir de uma tela antiga.

Pacientes, agenda, equipe e históricos aplicam paginação no banco, com filtros de autorização anteriores ao limite. Contrato continua array e exige adequação da navegação no frontend separado. Dashboard e exportação PDF ainda fazem agregações/carregamento em memória; precisam de revisão para grandes volumes. Lista de anexos tem limite, mas a entidade ainda carrega bytea ao buscar metadados; projeção SQL é otimização pendente, sem exposição desse conteúdo na API.

Tratamento global usa ErroApi com mensagem/campos sem incluir valores rejeitados, SQL, stacktrace ou conteúdo clínico. Abrange validação, JSON/parâmetro inválido, integridade, versão otimista, regras HTTP e tamanho de upload. Filtros de autenticação/CSRF continuam com erros servlet 401/403. Não se mascara exceção inesperada como erro de negócio.

OpenAPI: documentação e CSP específica somente em dev sem prod; rotas de documentação negadas nos demais perfis. Contrato estático em docs/openapi.yaml, exportado em teste sem dados clínicos. Detalhes em [swagger.md](swagger.md).

## Backlog priorizado após esta auditoria

| Prioridade | Pendência | Motivo |
|---|---|---|
| P0 | Confirmar Flyway, validate JPA, exclusões e corrida real em PostgreSQL no CI | H2 não substitui PostgreSQL; conferir resultado antes de integrar |
| P0 | Revisão clínica/OMS e LGPD operacional | Não há curvas validadas, gestão completa de consentimentos/retenção, recuperação de desastre ou auditoria inviolável |
| P1 | If-Match/versão esperada em edição de paciente e confirmação de transições | @Version protege concorrência transacional, não toda edição baseada em tela desatualizada |
| P1 | Navegação paginada no frontend separado | Evitar ocultar registros depois do limite de 50 |
| P1 | Antimalware/validação estrutural e cotas de armazenamento | Magic bytes não certificam segurança de PDF/PNG/JPEG |
| P1 | Sessões e rate limit distribuídos; auditoria de autenticação | Limitador e sessões atuais são em memória |
| P2 | Projeções de metadados de anexos e agregações SQL no dashboard | Reduzir bytea/memória e N+1 de contagens |
| P2 | DTOs como arquivos próprios e extração do transporte PDF | Records aninhados são contratos explícitos, mas há espaço para separar HTTP dos geradores |
| P2 | Módulos futuros estruturados | Pacotes package-info não representam financeiro, assinatura ou jogos implementados |

Antes de aplicar V3 em banco existente, consultar durações maiores que 8h e corrigir com revisão humana; a migration falha em vez de alterar horários silenciosamente:

```sql
select id, inicio, fim from consulta where fim - inicio > interval '8 hours';
```
