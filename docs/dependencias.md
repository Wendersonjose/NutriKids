# Dependências — revisão de 26/10/2026

A revisão não equivale a uma auditoria completa de vulnerabilidades ou de explorabilidade.

## Frontend

`npm audit --json` inicial identificou 3 pacotes afetados: Vitest e tinypool (críticos) e @vitest/mocker (moderado). Atualizado Vitest 3.2.7 para 5.0.5 e lockfile. A nova cadeia não contém tinypool. Reexecução da auditoria: **zero vulnerabilidades conhecidas reportadas**. Build TypeScript/Vite e 12 testes Vitest foram executados; consultar resultado consolidado em validacao.md.

Alertas reportados pelo registry:
- https://github.com/advisories/GHSA-82fw-gwwq-j7x9
- https://github.com/advisories/GHSA-qxg6-89mg-rqv8
- https://github.com/advisories/GHSA-5gmw-xhrv-c9v3
- https://github.com/advisories/GHSA-85c8-ppgw-ccpr

São ferramentas de desenvolvimento/teste; não afirmar que a vulnerabilidade esteve explorável no frontend publicado.

## Backend

Inspeção do JAR anterior identificou Tomcat 10.1.55, Jackson 2.21.4, Spring Framework 6.2.19, Spring Security 6.5.11, Flyway 11.7.2, driver PostgreSQL 42.7.11 e PDFBox 3.0.6.

Fontes primárias consultadas:
- https://tomcat.apache.org/security-10.html — Tomcat 10.1.61 corrige falhas que incluem versões 10.1.55 a 10.1.60.
- https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-m8xf-2cr7-77ph — correção na linha Jackson 2.21 em 2.21.8; a explorabilidade depende da configuração de polimorfismo e do caminho de entrada.
- https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-wv8q-qhhj-9h54
- https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-cxp5-3px4-pw24

O pom mantém Spring Boot 3.5.16 e Java 17; define overrides `tomcat.version=10.1.61` e `jackson-bom.version=2.21.8` para preservar a linha compatível com Jakarta/Spring Boot 3. A validação de resolução, compilação e regressão está registrada no relatório técnico. Não se habilitou default typing no Jackson nem se reproduziram exploits.

Limite: não foi executada varredura completa de todos os artefatos Java por OSV/NVD. O plugin Maven de árvore de dependências não pôde ser resolvido com a configuração de proxy herdada; a inspeção inicial usou diretamente BOOT-INF/lib do JAR. Uma nova tentativa de resolução sem o proxy herdado foi iniciada. Exigir scanner atualizado e revisão contínua antes de produção.
