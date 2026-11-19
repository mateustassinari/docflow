-- Dados fictícios para demonstração (perfil "demo"). Idempotente: rodar de novo não duplica nada.
-- As datas são relativas ao dia em que a migration roda, para a demo sempre ter tarefas
-- atrasadas, para hoje e futuras.

INSERT INTO clients (nome, cpf_cnpj, email, telefone) VALUES
    ('Mariana Costa', '111.111.111-11', 'mariana@exemplo.com', '(11) 90000-0001'),
    ('Rafael e Júlia Souza', '222.222.222-22', 'rafael@exemplo.com', '(11) 90000-0002'),
    ('Horizonte Eventos Ltda', '33.333.333/0001-33', 'contato@horizonte.exemplo.com', '(11) 3000-0003'),
    ('Carla Mendes', '444.444.444-44', 'carla@exemplo.com', '(11) 90000-0004')
ON CONFLICT (cpf_cnpj) DO NOTHING;

-- Eventos: um com sinal recebido, um sem sinal, um já realizado com saldo em atraso e uma encomenda quitada
INSERT INTO events (cliente_id, tipo_servico, tipo_evento, data, horario_inicio, duracao_estimada, local, numero_convidados,
                    valor, valor_sinal, forma_pagamento_sinal, forma_pagamento_restante,
                    sinal_recebido, data_recebimento_sinal, restante_recebido, data_recebimento_restante,
                    custo_total, custos_extras, contrato_dispensado, observacoes)
SELECT c.id, v.tipo_servico, v.tipo_evento, CURRENT_DATE + v.dias, v.horario, v.duracao, v.local, v.convidados,
       v.valor, v.sinal, 'PIX', 'PIX',
       v.sinal_recebido, CASE WHEN v.sinal_recebido THEN CURRENT_DATE - 10 END,
       v.restante_recebido, CASE WHEN v.restante_recebido THEN CURRENT_DATE - 3 END,
       v.custo, NULL, v.dispensado, v.obs
FROM (VALUES
    ('111.111.111-11', 'PINTURA_AO_VIVO', 'Casamento', 25, TIME '16:00', '5 horas', 'Espaço Jardim das Flores', 180,
     3500.00, 1000.00, TRUE, FALSE, NULL::numeric, FALSE, 'Cerimônia ao ar livre'),
    ('222.222.222-22', 'AQUARELA_AO_VIVO', 'Casamento', 60, TIME '19:00', '4 horas', 'Casa de Festas Aurora', 120,
     2800.00, 800.00, FALSE, FALSE, NULL::numeric, FALSE, NULL),
    ('33.333.333/0001-33', 'PINTURA_AO_VIVO', 'Corporativo', -12, TIME '20:00', '3 horas', 'Centro de Convenções', 300,
     4200.00, 1500.00, TRUE, FALSE, 900.00, FALSE, 'Confraternização de fim de ano'),
    ('444.444.444-44', 'ENCOMENDA_ARTISTICA', 'Encomenda', 15, NULL::time, NULL, 'Entrega por correio', NULL::int,
     1200.00, 400.00, TRUE, TRUE, 250.00, TRUE, 'Quadro de casamento em tela 60x80')
) AS v(cpf, tipo_servico, tipo_evento, dias, horario, duracao, local, convidados,
       valor, sinal, sinal_recebido, restante_recebido, custo, dispensado, obs)
JOIN clients c ON c.cpf_cnpj = v.cpf
WHERE NOT EXISTS (
    SELECT 1 FROM events e WHERE e.cliente_id = c.id AND e.tipo_evento = v.tipo_evento
);

-- Tarefas: atrasada, para hoje, em andamento (período), futura, concluída e uma avulsa
INSERT INTO tasks (event_id, titulo, descricao, prazo_data, prazo_data_fim, prazo_horario, prazo_horario_fim,
                   all_day, status, tipo_servico, created_at, updated_at)
SELECT e.id, v.titulo, v.descricao, CURRENT_DATE + v.inicio, CURRENT_DATE + v.fim, v.hora_inicio, v.hora_fim,
       FALSE, v.status, e.tipo_servico, NOW(), NOW()
FROM (VALUES
    ('111.111.111-11', 'Reunião com os noivos', 'Alinhar referências e paleta de cores', -5, NULL::int, TIME '10:00', TIME '11:00', 'A_FAZER'),
    ('111.111.111-11', 'Compra de material', NULL, 0, NULL::int, TIME '09:00', TIME '18:00', 'EM_ANDAMENTO'),
    ('111.111.111-11', 'Confirmar infraestrutura com cliente/cerimonialista', NULL, 18, NULL::int, TIME '09:00', TIME '18:00', 'A_FAZER'),
    ('444.444.444-44', 'Produção', 'Pintura da tela', -2, 3, TIME '09:00', TIME '18:00', 'EM_ANDAMENTO'),
    ('33.333.333/0001-33', 'Enviar pintura', NULL, -2, NULL::int, TIME '09:00', TIME '18:00', 'CONCLUIDA'),
    ('222.222.222-22', 'Organizar hospedagem', NULL, 45, NULL::int, TIME '09:00', TIME '18:00', 'A_FAZER')
) AS v(cpf, titulo, descricao, inicio, fim, hora_inicio, hora_fim, status)
JOIN clients c ON c.cpf_cnpj = v.cpf
JOIN events e ON e.cliente_id = c.id
WHERE NOT EXISTS (
    SELECT 1 FROM tasks t WHERE t.event_id = e.id AND t.titulo = v.titulo
);

INSERT INTO tasks (event_id, titulo, descricao, prazo_data, prazo_horario, prazo_horario_fim, all_day, status, created_at, updated_at)
SELECT NULL, 'Atualizar portfólio no Instagram', 'Postar fotos dos últimos eventos', CURRENT_DATE + 2, TIME '14:00', TIME '15:00',
       FALSE, 'A_FAZER', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE titulo = 'Atualizar portfólio no Instagram');

-- Checklist da compra de material e etiquetas
INSERT INTO task_checklist_items (task_id, texto, concluido, ordem)
SELECT t.id, v.texto, v.concluido, v.ordem
FROM tasks t
JOIN (VALUES ('Tela', TRUE, 0), ('Tintas', TRUE, 1), ('Moldura e Caixa?', FALSE, 2)) AS v(texto, concluido, ordem) ON TRUE
WHERE t.titulo = 'Compra de material'
  AND NOT EXISTS (SELECT 1 FROM task_checklist_items i WHERE i.task_id = t.id AND i.texto = v.texto);

INSERT INTO task_labels (task_id, label)
SELECT t.id, v.label
FROM tasks t
JOIN (VALUES ('Atualizar portfólio no Instagram', 'divulgação'), ('Reunião com os noivos', 'urgente')) AS v(titulo, label)
  ON t.titulo = v.titulo
WHERE NOT EXISTS (SELECT 1 FROM task_labels l WHERE l.task_id = t.id AND l.label = v.label);
