# Arquitetura

Monólito modular Java 17 / Spring Boot 3.5.16, API REST e React TypeScript/Vite. Pacotes seguem domínio. Módulos futuros possuem apenas package-info; isso não representa funcionalidade implementada. Prontuário concentra registros textuais de anamnese, plano, diário, terapia, mensagens e metas neste incremento; a separação em agregados estruturados é uma evolução planejada.

A sessão autenticada define usuário, clínica e perfil. O cliente não escolhe a clínica em requests de dados. Todas as consultas de recurso usam clinic_id da identidade autenticada; vínculos compostos no PostgreSQL impedem associações entre clínicas. Responsáveis acessam somente pacientes vinculados e registros explicitamente compartilhados. Equipe ADMIN/NUTRICIONISTA tem acesso clínico na própria clínica; ADMIN gere contas e vínculos. Não existe perfil de secretária nesta versão.

Cookies HttpOnly/SameSite Strict, CSRF por header, BCrypt custo 12 e limite local de tentativas. Senhas não retornam no JSON. No navegador, usar o proxy Vite `/api`; não guardar credenciais em localStorage. Em produção, servir frontend/API na mesma origem, usar HTTPS, COOKIE_SECURE=true e proxy reverso. Backend escuta localhost por padrão. Sessões e limitador são em memória; múltiplas instâncias exigem sessão compartilhada e limite distribuído.

Agenda representa intervalos [início,fim). PostgreSQL aplica exclusion constraints para profissional e paciente com status AGENDADA. Isso protege inclusive requisições concorrentes. Hibernate usa validate; Flyway aplica mudanças. H2 de teste não comprova exclusion constraints PostgreSQL.

Registros clínicos são acrescentados, sem endpoint de alteração/exclusão. Auditoria registra metadados de leitura/exportação e criação, sem copiar conteúdo de saúde. Auditoria não é armazenamento inviolável e ainda não cobre todos os eventos de autenticação ou mudanças administrativas futuras.

Uploads são armazenados no banco como bytea, até 5 MB, com título e compartilhamento. Download exige autorização por paciente e arquivo, como attachment e nosniff. Assinatura inicial de PDF/PNG/JPEG é verificada; não equivale a antimalware nem validação completa. Não há diretório público de anexos. Backups precisam incluir binários e ser criptografados externamente.

Não existem decisões clínicas automáticas. Medidas são registros; planos são orientações manuais do profissional. Não há afirmação de conformidade LGPD integral ou prontuário certificado.
