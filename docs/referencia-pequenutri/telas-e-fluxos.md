# Evidências visuais e fluxos

Nenhuma imagem do concorrente é distribuída com o frontend. Links servem para rastreabilidade. As duas capturas do site são exemplos divulgados pelo fornecedor, não sessões reais inspecionadas. Os registros de pacientes nelas não foram reutilizados como dados do projeto.

## Capturas analisadas visualmente

| Fonte / imagem | Tela | Elementos observados | Fluxo sugerido pela interface | Limite |
|---|---|---|---|---|
| [Agenda Android](https://pequenutri.com.br/_astro/tela-agenda.BoEHet95_Z2lbxnh.webp) | Agenda móvel | Semana navegável, faixa de horários, cartões, responsável, idade, modalidade, status, botão novo agendamento | Escolher dia → abrir consulta ou novo agendamento | Formulário do agendamento não visível |
| [Início iPhone](https://pequenutri.com.br/_astro/tela-inicio.DKHDnGC6_WCtr2.webp) | Dashboard móvel | Saudação, contagem de consultas, atendimento atual, ver ficha, iniciar atendimento, etapas pré-atendimento/atendimento, planejar consulta e continuar | Início → ficha ou condução de atendimento | Não comprova regras internas das etapas |
| [Screenshot Google Play 1](https://play-lh.googleusercontent.com/fDTIYj3i6o5kNjz6tCqdd8BotTxMDkz-KbzwDnho0PhHSsBQ--jyTFD956b9TKoOGweVM5E2Zo4OvW4JumZEpg=w1000) | Atividade de aproximação alimentar | Ilustração de criança, busca de alimento, filtros por grupos, cartões de alimentos, voltar | Buscar/filtrar → selecionar alimento | Tela de atividade; resultado após seleção não observado |

Na navegação móvel das duas capturas do site aparecem Início, Pacientes, Agenda e Mais. Não há evidência visual suficiente para reconstruir um menu lateral desktop. Identidade divulgada usa magenta, verde-azulado e materiais ilustrados. Nossa interface usa verde profundo e tons neutros, estrutura e textos próprios.

## Inventário de imagens das lojas

As 15 imagens estão ligadas na [página oficial Google Play](https://play.google.com/store/apps/details?id=com.pequenutri.app), links de screenshots 11–25 no extrator. Foram resolvidas durante a pesquisa; detalhes ilegíveis nas miniaturas não foram convertidos em afirmações de campos ou regras. Lista de URLs em `imagens-publicas.md`.

## Cobertura das telas solicitadas

| Tela | Estado da evidência |
|---|---|
| Login | HTML Flutter público; campos renderizados não verificados |
| Dashboard | Captura móvel observada |
| Menu e navegação | Barra inferior móvel observada; lateral desktop não verificada |
| Cadastro e ficha do paciente | Cadastro declarado; formulário não observado |
| Prontuário e anamnese | Declarados; campos internos não observados |
| Agenda | Captura móvel observada |
| Antropometria e curvas | Declaradas; gráficos específicos não inspecionados |
| Plano e relatórios | Declarados; composição final não verificada |
| Diário e portal familiar | Descritos pelas lojas; acesso autenticado não realizado |
| Aplicativo móvel | Lojas e capturas oficiais |
| Jogos e gamificação | Captura de atividade observada; biblioteca anunciada |
| Financeiro, configurações, assinaturas | Recursos ou fluxo comercial declarados; telas internas não verificadas |

## Fluxos confirmados por descrição pública

S1 descreve cadastro comercial seguido de pagamento e entrada na plataforma. S4/S5 descrevem acesso familiar por convite do profissional. A política S6 diferencia equipe clínica e responsáveis e informa que a criança não possui login próprio. Nosso bootstrap administrativo e cadastro manual de responsáveis são simplificações explicitamente distintas desse convite comercial.
