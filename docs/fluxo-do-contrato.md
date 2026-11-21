---
hide:
  - navigation
---

# Fluxo do contrato

O fluxo principal do sistema vai do contrato gerado até as tarefas do evento.

1. A usuária cadastra o cliente e o evento, e **gera o contrato** a partir do template do tipo de serviço. O PDF é gerado com o OpenPDF e guardado na pasta de arquivos.
2. Ela revisa o PDF e **envia para assinatura**. A API manda o PDF ao Autentique, que envia o contrato ao cliente por e-mail.
3. O cliente assina no Autentique.
4. A cada 30 minutos, um **agendador** verifica os contratos aguardando assinatura. O Autentique não avisa a API (não há webhook), então a API pergunta.
5. Quando encontra um contrato assinado, a API:
    - baixa o PDF assinado e o guarda no lugar do original;
    - marca o contrato como assinado;
    - **cria as tarefas do evento**, conforme o tipo de serviço (por exemplo: reunião D-30, compra de material D-15, entrega em dias úteis após a assinatura);
    - envia um **e-mail avisando** que o contrato foi assinado.

<figure markdown="span">
  ![Sequência: o que acontece quando o agendador encontra um contrato assinado.](diagramas/FluxoDoContrato.svg)
  <figcaption>Sequência: o que acontece quando o agendador encontra um contrato assinado.</figcaption>
</figure>

## Lembretes de prazo

Todo dia às 8h (e também quando a aplicação sobe), outro agendador procura tarefas que vencem **em 7 dias, em 3 dias e no próprio dia** e envia um e-mail com a lista. Cada aviso é registrado no banco, então o mesmo lembrete não é enviado duas vezes.

## Tarefas encadeadas

Algumas tarefas criam outras quando concluídas. Por exemplo, concluir o item "Moldura e Caixa?" de uma pintura cria as tarefas de retoque, emoldurar e envio.
