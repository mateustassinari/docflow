# DocFlow

[![CI](https://github.com/mateustassinari/docflow/actions/workflows/ci.yml/badge.svg)](https://github.com/mateustassinari/docflow/actions/workflows/ci.yml)

Gestão de contratos, eventos e operação para negócios de arte em eventos (pintura ao vivo, aquarela, encomendas e identidade visual). Ele substitui um fluxo manual (documentos soltos, plataforma de assinatura e lembretes "de cabeça") por um processo integrado, do contrato à entrega.

O sistema nasceu para um negócio real e está em uso. Este repositório é a versão pública: os dados do negócio ficam na configuração, e o código vem com dados de demonstração.

## O que ele faz

- **Contratos em PDF** gerados a partir de templates por tipo de serviço, com placeholders (`{{clienteNome}}`, `{{dadosServico.*}}`) e cláusulas condicionais.
- **Assinatura eletrônica** pelo [Autentique](https://www.autentique.com.br): envio do contrato, sincronização periódica do status, download do PDF assinado e aviso por e-mail.
- **Tarefas automáticas após a assinatura**, por tipo de serviço, com prazos relativos à data do evento (reunião D-30, compra de material D-15...) ou à data da assinatura (entrega em dias úteis). Algumas são encadeadas: concluir o item "Moldura e Caixa?" cria retoque, emoldurar e envio.
- **Tarefas e agenda:** calendário, kanban, checklist, comentários, anexos, etiquetas, PDF de agenda (mês, semana e dia) e lembretes diários por e-mail (7 dias, 3 dias e no dia).
- **Financeiro:** status de pagamento (sinal e saldo) e custos por evento.
- **Assistente de IA (opcional)** com modelo local via [Ollama](https://ollama.com): chat e insights sobre o negócio, sem enviar dados para fora da máquina.

## Fluxo principal

```
Cliente + Evento ──▶ Contrato (PDF do template) ──▶ Autentique ──▶ assinado
                                                                    │
                     Tarefas do evento ◀── automação por tipo de serviço
                     (prazos relativos) ──▶ lembretes por e-mail
```

## Arquitetura

Monolito em camadas, inspirado em Clean Architecture (ports & adapters):

```
presentation ──► application ──► domain ◄── infrastructure
 (REST, DTOs)    (casos de uso,    (modelos,      (JPA, OpenPDF, Autentique,
                  portas)           regras,        SMTP, Ollama, arquivos)
                                    portas)
```

- O **domínio** não depende de Spring nem de JPA.
- A **aplicação** fala com o mundo externo por portas (`TaskRepository`, `ContractDocumentGenerator`, `TaskAttachmentStorage`...) e recebe comandos, não DTOs HTTP.
- A regra de dependência é **verificada por teste** (`ArchitectureTest`, com ArchUnit). Quem a quebrar recebe na mensagem de erro o motivo e o que fazer.
- **Autenticação com Spring Security**, por sessão no servidor e cookie `HttpOnly`, com proteção CSRF. O motivo da escolha (e por que não JWT) está no [ADR 1](docs/adr/0001-autenticacao-com-sessao-e-cookie.md).

Um monolito é intencional: é um negócio com uma usuária, e distribuir o sistema traria custo sem benefício.

## Como rodar

### Com Docker (recomendado)

Requisito: Docker com Compose.

```bash
cp .env.example .env        # opcional: sem ele, usa os padrões
docker compose up --build
```

Acesse **http://localhost:8081** e entre com o usuário **`demo`** e a senha **`demo`**. Por padrão, só este computador acessa (`BIND_ADDRESS=127.0.0.1`); para abrir na rede local, troque a senha antes. O compose sobe Postgres, backend e frontend (nginx), com o perfil `demo`, que carrega clientes, eventos e tarefas fictícios.

Para apagar tudo, inclusive o banco:
```bash
docker compose down -v
```

### Sem Docker

Requisitos: Java 17+ para rodar o Gradle (ele baixa o JDK 25 do projeto se faltar), Node 24+ e um Postgres com banco, usuário e senha `docflow` (ou ajuste `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`).

```bash
APP_PASSWORD=demo ./gradlew bootRun                 # backend em http://localhost:8080 (usuário admin)
cd frontend-web && npm ci && npm run dev            # frontend em http://localhost:5173
```

O Vite repassa `/api` para o backend, então frontend e API ficam na mesma origem, como no Docker.

Para carregar os dados de demonstração, use `SPRING_PROFILES_ACTIVE=demo`.

## Configuração

Tudo é configurado por variáveis de ambiente (veja [`.env.example`](.env.example)). Sem nenhuma conta externa, o sistema funciona com cadastros, contratos em PDF, tarefas, calendário e financeiro. As integrações ligam quando você informa as **suas** credenciais:

| Integração | Variáveis | Sem configurar |
| --- | --- | --- |
| Acesso (login) | `APP_USERNAME`, `APP_PASSWORD`, `SESSION_TIMEOUT`, `BIND_ADDRESS` | No compose, `demo` / `demo`; fora dele, a aplicação não sobe sem `APP_PASSWORD` |
| Identidade do negócio | `APP_BUSINESS_NAME`, `APP_OWNER_NAME`, `APP_SIGNATURE_IMAGE` | Usa "DocFlow" e PDF sem imagem de assinatura |
| Assinatura (Autentique) | `AUTENTIQUE_API_TOKEN`, `AUTENTIQUE_ACCOUNT_EMAIL`, `AUTENTIQUE_SANDBOX_ENABLED` | Envio para assinatura indisponível (o sandbox é gratuito) |
| E-mail (SMTP) | `EMAIL_NOTIFICATIONS_ENABLED`, `EMAIL_NOTIFICATIONS_TO`, `SMTP_*` | Lembretes e avisos não são enviados |
| Assistente de IA | `AI_ENABLED`, `OLLAMA_URL`, `OLLAMA_MODEL` | O item "Assistente IA" não aparece no menu |

No Docker, para usar o Ollama da sua máquina:
```bash
AI_ENABLED=true
OLLAMA_URL=http://host.docker.internal:11434
```

## Segurança

- **Login com sessão** e cookie `HttpOnly`/`SameSite=Lax`, proteção **CSRF** em toda escrita e senha guardada só como hash BCrypt ([ADR 1](docs/adr/0001-autenticacao-com-sessao-e-cookie.md)).
- **Limite de tentativas de login** no nginx (5 por minuto por IP).
- **Cabeçalhos de segurança** no nginx: CSP, `X-Frame-Options`, `nosniff`, `Referrer-Policy`.
- **Anexos tratados como não confiáveis:** nome escapado no download, só tipos de uma lista permitida, gravação com nome gerado pelo servidor.
- **Docker:** só o nginx é publicado, e só em `127.0.0.1` por padrão; o backend roda sem root e não consegue alterar o próprio jar.

## Testes

```bash
./gradlew test
```

Os testes incluem:
- **Arquitetura** (ArchUnit): regra de dependência entre camadas;
- **Domínio e aplicação**, com repositórios em memória e relógio fixo (`Clock` injetado);
- **Persistência** com Postgres real via Testcontainers, rodando as migrations Flyway (**precisa do Docker**);
- **Contrato JSON** da API de tarefas e **conteúdo dos PDFs** (pelo texto extraído).

O CI (GitHub Actions) roda os testes, o lint e o build do frontend e o build das imagens Docker a cada push e pull request.

## Stack

Java 25 · Spring Boot 4.2 · Spring Data JPA · PostgreSQL · Flyway · OpenPDF · JUnit 6 · AssertJ · ArchUnit · Testcontainers · React 19 · Vite · FullCalendar · Docker Compose · nginx · GitHub Actions