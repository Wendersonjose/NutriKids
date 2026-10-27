# NutriKids — Frontend

React + TypeScript + Vite + React Router. Backend separado: https://github.com/Wendersonjose/NutriKids.

## Windows / PowerShell

Pré-requisitos: Node.js 22.12+ e npm. Inicie o backend na porta 8080 seguindo o README daquele repositório.

Nesta pasta:

```powershell
npm ci
npm test
npm run build
npm run dev
```

Abra http://localhost:5173. O Vite encaminha `/api` para http://127.0.0.1:8080. Login usa sessão HttpOnly e CSRF; não gravar tokens/senhas em localStorage. O backend deve ter sido inicializado com clínica e administrador.

Na primeira entrada, configure a clínica, cadastre equipe/responsável, paciente e vínculo. O portal familiar usa a mesma tela de login com as permissões da conta. Informações clínicas só aparecem conforme autorização da API.

## Funcionalidades

Dashboard com contagens da API; cadastro/ficha/edição de pacientes; agenda; registros textuais de anamnese, prontuário, plano manual, diário, terapia, metas e mensagens; medidas; anexos protegidos; PDF; clínica/equipe; vínculo de responsáveis; login/logout. Estados de carregamento, erros e sessão expirada. Layout responsivo.

## Validação e limites

9 testes Vitest passaram, TypeScript e build Vite concluíram. 8 verificações HTTP com o backend passaram em banco H2 sintético. PostgreSQL e migrações precisam de validação no ambiente local; nenhum dado real foi usado. Testes de componentes usam API simulada; o teste HTTP separado usou os serviços reais. Inspeção visual final em navegador desktop e celular não foi concluída.

Anamnese e planos são textos manuais, mensagens são assíncronas; não há cálculos de nutrientes, curvas OMS, jogos, financeiro, assinatura, recuperação de senha ou notificações. Não usar com dados reais antes das etapas de segurança e validação descritas no backend.

## Produção

Servir `dist/` e encaminhar `/api` para o backend na mesma origem via proxy reverso, com HTTPS e COOKIE_SECURE=true. Configurar fallback das rotas React para index.html. O servidor Vite é apenas de desenvolvimento. Não habilitar cache offline de dados clínicos por padrão. Esta entrega não foi implantada em produção.
