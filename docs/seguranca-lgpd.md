# Segurança e operação com dados sensíveis

Implementado: controle de clínica, perfil e vínculo por paciente; hash de senha; CSRF; cookie de sessão; validação; auditoria de recursos; no-store em respostas; PDF e arquivos autenticados; nenhuma informação real nos testes.

## Política operacional a aprovar pela clínica

- Designar controlador, operador, encarregado/canal de contato e responsáveis pela autorização de acessos.
- Documentar finalidades e bases legais por operação, incluindo dados da criança e autorização do responsável. Não usar um único checkbox genérico como substituto dessa análise.
- Antes de dados reais: implementar registro de aceite/consentimento quando aplicável com versão, finalidade, data e responsável; revogação com efeitos definidos.
- Definir matriz de retenção por tipo de dado, considerando obrigações profissionais aplicáveis. Não apagar prontuário automaticamente ao excluir conta.
- Pedidos de acesso, correção, portabilidade e exclusão: confirmar identidade e autoridade sobre a criança, analisar obrigação de conservação, registrar decisão e executar com trilha. Automação desses direitos ainda pendente.
- Backups criptografados, teste de restauração, retenção de backups e acesso mínimo de operadores.
- Inventário de fornecedores, avaliação de transferência internacional e plano de resposta a incidentes.

## Barreiras antes de produção

Recuperação/troca de senha, revogação de acesso, consentimento versionado, MFA administrativo, revisão clínica, teste real PostgreSQL, restauração de backup, limites globais e por clínica, antimalware e revisão de ameaças ainda precisam ser concluídos. Sem essas etapas, usar somente dados sintéticos. A implantação pública não foi realizada.
