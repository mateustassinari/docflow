---
hide:
  - navigation
---

# ADR 1: Autenticação com sessão no servidor e cookie

!!! success "Aceita em 20/11/2026"

## Contexto

O DocFlow não tinha autenticação: qualquer pessoa que alcançasse a API conseguia ler e alterar clientes, contratos e tarefas. Enquanto o sistema rodava só na máquina da usuária, isso era tolerável. Com o repositório público, o Docker e a perspectiva de acesso por outros dispositivos, deixou de ser.

Características que pesam na escolha:

- **Uma usuária só.** Não há cadastro, papéis nem permissões diferentes.
- **Um único cliente:** o frontend React deste repositório. Não há aplicativo móvel, integrações de terceiros nem outros serviços chamando a API.
- **Frontend e API na mesma origem.** No Docker, o nginx entrega o React e repassa `/api` para o backend; em desenvolvimento, o Vite faz o mesmo. Para o navegador, existe um endereço só.
- **PDFs abertos direto pelo navegador** (`window.open` no contrato e na agenda), sem passar pelo cliente HTTP do React.
- **Monolito**, com uma instância do backend.

## Decisão

Usar **Spring Security com login por formulário e sessão no servidor**:

- `POST /auth/login` (usuário e senha em `application/x-www-form-urlencoded`) cria a sessão. O navegador recebe o cookie `JSESSIONID` com `HttpOnly` e `SameSite=Lax`. `POST /auth/logout` encerra a sessão.
- `GET /auth/session` diz ao frontend se há sessão e com qual usuário. É a única rota liberada sem login.
- **Proteção CSRF** no padrão para SPAs (`csrf.spa()`): o token vai num cookie `XSRF-TOKEN` legível pelo JavaScript e volta no header `X-XSRF-TOKEN` em toda escrita. O token é trocado no login e no logout.
- Toda resposta de segurança é **JSON no formato de erro da API** (401 sem login ou com senha errada, 403 sem token CSRF). Nada de redirecionamento ou página HTML de login do Spring: a tela é do React.
- **Usuário único vindo da configuração** (`APP_USERNAME`, `APP_PASSWORD`). A senha em texto puro recebe hash BCrypt ao subir; também é aceito um hash pronto (`{bcrypt}...`). Sem senha definida, a aplicação não sobe.
- **Sem CORS.** Com o proxy em desenvolvimento, frontend e API ficam na mesma origem nos dois ambientes, e a configuração de CORS foi removida.

## Alternativas consideradas

**JWT (token no header `Authorization`).** É a opção mais citada para APIs REST, mas os benefícios dela não se aplicam aqui:

- Brilha quando vários clientes ou serviços precisam validar o token sem consultar o servidor de sessão. Aqui há um cliente e um backend.
- O token precisa ficar em algum lugar do navegador. No `localStorage`, ele fica exposto a qualquer script injetado na página (XSS). Num cookie, voltamos ao modelo de sessão, só que com mais peças.
- Revogar um JWT antes de expirar (logout de verdade, troca de senha) exige uma lista de bloqueio no servidor, ou seja, estado, que é o que o JWT promete evitar.
- Os PDFs abertos com `window.open` não carregam header `Authorization`. Cada um teria de ser baixado via `fetch` e transformado em blob.
- Mais código para manter: emissão, validação, expiração e renovação (refresh token).

**HTTP Basic.** Simples, mas o navegador mostra uma janela própria de login, não há logout de verdade e as credenciais vão em toda requisição.

**Provedor de identidade (OAuth2/OIDC, como Keycloak).** Adequado para vários sistemas com login único entre eles. Para uma usuária e um sistema, é uma infraestrutura inteira a mais para operar.

**Continuar sem autenticação.** Inaceitável com o sistema acessível por rede.

## Consequências

Positivas:

- O cookie de sessão é `HttpOnly`: um script injetado na página não consegue lê-lo nem enviá-lo para fora.
- Logout e expiração são imediatos e controlados pelo servidor (`SESSION_TIMEOUT`, padrão de 8 horas sem uso).
- Os PDFs e downloads funcionam sem mudança, porque o navegador envia o cookie sozinho. Para a pré-visualização em `iframe`, o `X-Frame-Options` padrão do Spring Security (`DENY`) foi trocado por `SAMEORIGIN`.
- Pouco código: login, logout, sessão e CSRF vêm prontos do Spring Security, configurados em `SecurityConfig`.

Negativas e limites aceitos:

- **As sessões ficam na memória do backend.** Reiniciar o servidor desloga a usuária, e duas instâncias do backend não compartilhariam sessões. Se um dia for preciso, o Spring Session guarda as sessões no Postgres sem mudar o resto.
- **O limite de tentativas de login fica no nginx** (5 por minuto por IP, com folga para 5 seguidas; depois, 429). Rodando sem Docker, não há limite: o backend sozinho não conta tentativas.
- **O frontend precisa estar na mesma origem da API.** Hospedar o frontend em outro domínio exigiria voltar com o CORS, já com `allowCredentials`.
- **Em produção, é preciso HTTPS** (`SESSION_COOKIE_SECURE=true`): sem ele, o cookie de sessão trafega em texto puro na rede.
