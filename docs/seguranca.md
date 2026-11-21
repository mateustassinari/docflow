---
hide:
  - navigation
---

# Segurança

## Autenticação

- **Login com sessão no servidor** e cookie `HttpOnly` e `SameSite=Lax`, com expiração por inatividade. O motivo da escolha (e por que não JWT) está no [ADR 1](adr/0001-autenticacao-com-sessao-e-cookie.md).
- **Proteção CSRF** em toda escrita: o token vai num cookie legível pelo JavaScript e volta no header `X-XSRF-TOKEN`.
- **Usuário único** vindo da configuração; da senha, só o hash BCrypt fica em memória. Sem senha definida, a aplicação não sobe.
- **Limite de tentativas de login** no nginx: 5 por minuto por IP; depois, `429`.

## Navegador

- Frontend e API na **mesma origem**, sem CORS: nenhum outro site consegue fazer chamadas com o cookie de sessão.
- **Cabeçalhos de segurança**: Content-Security-Policy (só scripts da própria origem), `X-Frame-Options: SAMEORIGIN`, `nosniff`, `Referrer-Policy` e `Permissions-Policy`.

## Arquivos

- PDFs e anexos são gravados com **nome gerado pelo servidor** (UUID), sem usar caminhos vindos do usuário.
- O upload de contrato só aceita **PDF de verdade** (conferido pelo conteúdo, não pela extensão).
- Anexos são tratados como **não confiáveis**: o nome é escapado no download, e só tipos de uma lista permitida são devolvidos como vieram; os demais saem como download genérico.
- A API **não expõe caminhos internos** do servidor.

## Dados e erros

- Acesso ao banco só por **consultas com parâmetros** (JPA), sem concatenação de SQL.
- Erros inesperados devolvem uma mensagem genérica; o detalhe fica só no log.
- Campos de texto com **limite de tamanho**, alinhado às colunas do banco.

## Docker

<figure markdown="span">
  ![Implantação: como o docker compose distribui o sistema e o que fica exposto.](diagramas/Implantacao.svg)
  <figcaption>Implantação: como o docker compose distribui o sistema e o que fica exposto.</figcaption>
</figure>

- Só o nginx é publicado, e só em `127.0.0.1` por padrão; o banco e a API ficam na rede interna do compose.
- A API roda **sem root** e só consegue escrever na pasta de uploads.
- As imagens aplicam as correções de segurança do sistema operacional no build.

## Fora do escopo, por decisão

- **Várias usuárias e permissões**: o sistema tem uma usuária só. Se isso mudar, cada consulta vai precisar filtrar pelo dono dos dados (proteção contra IDOR).
- **HTTPS**: fica a cargo de quem expuser o sistema fora da máquina, com `SESSION_COOKIE_SECURE=true`.
