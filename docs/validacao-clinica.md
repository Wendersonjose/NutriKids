# Validação clínica e composição — 26/10/2026

Estado: implementação experimental com testes de referência; **não homologada para uso clínico real**. Não há diagnóstico, prescrição automática, estimativa de necessidade energética ou correção por prematuridade.

## Referência OMS 2006

Fontes consultadas em 26/10/2026:
- https://www.who.int/toolkits/child-growth-standards/
- https://www.who.int/toolkits/child-growth-standards/software
- https://github.com/WorldHealthOrganization/anthro — commit `b776d8a12b1c97369c748b561159fd2ec4f4db58`.
- https://www.who.int/docs/default-source/child-growth/growth-reference-5-19-years/computation.pdf
- https://www.who.int/tools/growth-reference-data-for-5to19-years/application-tools
- https://www.gov.br/saude/pt-br/composicao/saps/pnan/acoes-estrategicas-da-pnan/vigilancia-alimentar-e-nutricional-1/parametros-para-avaliacao-nutricional

Tabelas diárias LMS `weianthro`, `lenanthro`, `bmianthro` e tabelas por décimo de centímetro `wflanthro`/`wfhanthro`. Origem, licença GPL-3 upstream e hashes estão em `src/main/resources/oms`. A atribuição não substitui a análise das obrigações de distribuição do conjunto com o SaaS.

População implementada: ambos os sexos de referência F/M, idade em dias de calendário, idade/30,4375 < 60 meses. Sexo não informado e indicadores fora da faixa retornam resultado indisponível com motivo; não são extrapolados. Último dia aceito: 1826; 1827 é recusado. Não confundir este recorte operacional do pacote anthro com validação completa de todas as idades.

- LMS: Z=((X/M)^L−1)/(L*S); em L=0 usa ln(X/M)/S.
- Indicadores baseados em peso: extensão além de ±3 pelo espaçamento entre ±2 e ±3, conforme metodologia publicada. Comprimento/altura por idade usa LMS sem essa extensão.
- Menores de 731 dias: referência de comprimento; medida em pé recebe +0,7 cm. A partir de 731 dias: referência de altura; medida deitada recebe −0,7 cm. IMC usa a dimensão ajustada.
- Peso por comprimento: 45–110 cm; peso por altura: 65–120 cm. Interpolação linear de L, M e S entre décimos somente nesse indicador, seguindo rotina oficial `anthro`; não se interpolam idades das tabelas diárias.
- Edema precisa ser informado explicitamente no endpoint. Com edema, índices baseados em peso ficam indisponíveis. A consulta atual aplica essa informação a todo o histórico; edema por avaliação ainda deve ser modelado antes do uso clínico.
- Sinalizadores para revisão: peso/idade Z < −6 ou > 5; comprimento/idade |Z| > 6; IMC e peso/comprimento |Z| > 5. São alertas de plausibilidade, não diagnósticos.
- Z retornado arredondado em duas casas; percentil vem da normal acumulada aplicada ao Z não arredondado. Aproximação Abramowitz–Stegun 26.2.17, erro absoluto da CDF < 7,5×10⁻⁸ (erro percentual < 0,0000075 ponto percentual).
- O gráfico mostra evolução de Z por sequência de avaliações e linhas de referência Z. Não é um gráfico de medidas físicas por idade, não é espaçado por tempo e não é uma reprodução do WHO Anthro.

## Evidências automatizadas

| Caso do pacote oficial anthro | Resultado esperado | Java | Diferença em Z arredondado |
|---|---:|---:|---:|
| Menina, 1522 dias, 17 kg, peso/idade (`test-zscore-weight-for-age.R`) | 0,24 | 0,24 | 0,00 |
| Menino, 44 dias, 50 cm deitado, comprimento/idade (`test-zscore-length-for-age.R`) | −3,29 | −3,29 | 0,00 |

Outros testes: medianas de nascimento em ambos os sexos, L=0, CDF em Z=0 e Z=2, edema, extremo de peso, posições nos dias 730/731, limite etário, comprimento fora da tabela, sexo desconhecido e NaN.

Limite da evidência: comparação com resultados de testes oficiais publicados, **sem executar os binários WHO Anthro/AnthroPlus nem o pacote R nesta máquina**. Ainda faltam vetores independentes para todos os indicadores, extremos em ambos os sexos, malha completa de idades/comprimentos, auditoria numérica e assinatura do nutricionista revisor. OMS 2007/5–19 anos e validação AnthroPlus não foram implementadas nesta entrega.

## Composição dos alimentos

Fontes consultadas:
- https://tbca.net.br/base-dados/
- https://nepa.unicamp.br/publicacoes/ (links oficiais TACO Excel/PDF)
- https://www.gov.br/saude/pt-br/composicao/saps/promocao-da-saude/guias-alimentares/guias-alimentares

Não foi confirmada autorização para redistribuição comercial em massa de TBCA/TACO; **nenhuma dessas bases foi importada**. Catálogo vazio e cadastro manual por profissional com fonte, versão e declaração de permissão obrigatórias. A declaração é um registro de responsabilidade, não uma verificação automática da licença. Dados sintéticos aparecem apenas nos testes.

Composição por 100 g da parte comestível. Porção = composição×gramas/100, BigDecimal, seis casas, HALF_EVEN. Medidas caseiras exigem peso cadastrado por alimento; não há conversão universal inventada. Chaves explicitam unidade (`energia_kcal`, `proteinas_g`, `ferro_mg`, etc.); a API ainda exige disciplina do cadastrador para padronização das chaves. Nutrientes ausentes não são tratados como zero: totais contêm apenas a interseção dos nutrientes informados em todos os itens.

Refeições e total diário utilizam soma decimal. O motor de rendimento de preparação calcula total/rendimento medido×100 g; não presume rendimento, retenção de vitaminas ou perdas por cozimento. Esse motor possui teste unitário, mas editor e biblioteca persistente de receitas ainda estão pendentes.

Planos calculados preservam versões como registros textuais imutáveis no histórico existente, com quantidades, nomes, fontes, composição por refeição e total diário. O PDF existente inclui esses textos. Editar no frontend e salvar cria nova versão. Não há equivalência clínica automática de substituições, edição estruturada de uma versão histórica ou incorporação das curvas no PDF.

## Liberação clínica pendente

Revisão de tabelas/licenças, validação numérica ampliada, população especial/prematuridade/edema por medição, revisão dos dados alimentares, terminologia, protocolo de interpretação e aceite formal do responsável técnico. Testes de software não equivalem a validação clínica.
