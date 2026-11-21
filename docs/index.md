---
hide:
  - navigation
---

# Visão geral

O DocFlow substitui um fluxo manual de um negócio de arte em eventos (pintura ao vivo, aquarela, encomendas e identidade visual): documentos soltos, plataforma de assinatura à parte e lembretes "de cabeça". Ele integra o processo do contrato à entrega.

## Quem usa

Uma pessoa: a dona do negócio. Os clientes não acessam o sistema; eles recebem o contrato e assinam pelo Autentique.

<figure markdown="span">
  ![Contexto: quem usa o DocFlow e com quais sistemas ele conversa.](diagramas/Contexto.svg)
  <figcaption>Contexto: quem usa o DocFlow e com quais sistemas ele conversa.</figcaption>
</figure>

## O que o sistema faz

- **Contratos em PDF** gerados a partir de templates por tipo de serviço, com placeholders e cláusulas condicionais.
- **Assinatura eletrônica** pelo Autentique, com sincronização periódica do status.
- **Tarefas automáticas** criadas quando o contrato é assinado, com prazos relativos à data do evento ou da assinatura.
- **Agenda de tarefas** em lista, kanban e calendário, com PDF da agenda e lembretes diários por e-mail.
- **Financeiro**: sinal, saldo e custos por evento.
- **Assistente de IA opcional**, com modelo local (nenhum dado sai da máquina).

## Containers

O frontend e a API ficam na **mesma origem**: o nginx entrega o React e repassa `/api` para a API. Isso dispensa CORS e permite autenticação por sessão com cookie (veja o [ADR 1](adr/0001-autenticacao-com-sessao-e-cookie.md)).

<figure markdown="span">
  ![Containers: as peças que rodam separadas e como se comunicam.](diagramas/Containers.svg)
  <figcaption>Containers: as peças que rodam separadas e como se comunicam.</figcaption>
</figure>

## Arquitetura da API

A API é um **monolito em camadas**, inspirado em Clean Architecture (ports & adapters):

```
presentation ──► application ──► domain ◄── infrastructure
```

- O **domínio** não depende de Spring nem de JPA.
- A **aplicação** fala com o mundo externo por **portas** (interfaces como `SignatureService`, `NotificationService`, `TaskRepository`), e a infraestrutura as implementa.
- A regra de dependência é **verificada por teste**: o `ArchitectureTest` (ArchUnit) falha o build se uma camada depender de outra que não pode.

Nos diagramas abaixo, as setas da aplicação para a infraestrutura mostram as chamadas em tempo de execução; no código, essas chamadas passam pelas portas indicadas em cada seta. A API aparece em três recortes, para cada diagrama continuar legível.

### Contratos

<figure markdown="span">
  ![Componentes da API: o caminho do contrato, da geração à assinatura.](diagramas/ComponentesContratos.svg)
  <figcaption>Componentes da API: o caminho do contrato, da geração à assinatura.</figcaption>
</figure>

### Tarefas e lembretes

<figure markdown="span">
  ![Componentes da API: tarefas, anexos, agenda em PDF e lembretes de prazo.](diagramas/ComponentesTarefas.svg)
  <figcaption>Componentes da API: tarefas, anexos, agenda em PDF e lembretes de prazo.</figcaption>
</figure>

### Assistente de IA

<figure markdown="span">
  ![Componentes da API: o assistente de IA, opcional, com modelo local.](diagramas/ComponentesIA.svg)
  <figcaption>Componentes da API: o assistente de IA, opcional, com modelo local.</figcaption>
</figure>

## Por que um monolito

É um negócio com uma usuária. Dividir em serviços traria custo de operação (deploy, rede, consistência entre bancos) sem benefício. As camadas e o teste de arquitetura dão a separação de responsabilidades que importa aqui.
