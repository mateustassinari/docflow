ALTER TABLE templates_contrato
    ALTER COLUMN caminho_arquivo DROP NOT NULL;

ALTER TABLE templates_contrato
    ADD COLUMN IF NOT EXISTS descricao TEXT,
    ADD COLUMN IF NOT EXISTS cidade_assinatura VARCHAR(120),
    ADD COLUMN IF NOT EXISTS titulo_documento VARCHAR(255),
    ADD COLUMN IF NOT EXISTS subtitulo_documento VARCHAR(255),
    ADD COLUMN IF NOT EXISTS clausula_objeto TEXT,
    ADD COLUMN IF NOT EXISTS clausula_servico TEXT,
    ADD COLUMN IF NOT EXISTS clausula_visao_artistica TEXT,
    ADD COLUMN IF NOT EXISTS clausula_pagamento TEXT,
    ADD COLUMN IF NOT EXISTS clausula_deslocamento TEXT,
    ADD COLUMN IF NOT EXISTS clausula_cancelamento TEXT,
    ADD COLUMN IF NOT EXISTS clausula_obrigacoes_contratada TEXT,
    ADD COLUMN IF NOT EXISTS clausula_obrigacoes_contratante TEXT,
    ADD COLUMN IF NOT EXISTS clausula_direitos_imagem TEXT,
    ADD COLUMN IF NOT EXISTS clausula_disposicoes_gerais TEXT,
    ADD COLUMN IF NOT EXISTS contratada_nome VARCHAR(255),
    ADD COLUMN IF NOT EXISTS contratada_documento VARCHAR(120),
    ADD COLUMN IF NOT EXISTS contratada_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS contratada_telefone VARCHAR(120);

UPDATE templates_contrato
SET caminho_arquivo = NULL,
    descricao = COALESCE(descricao, 'Modelo base para pintura ao vivo em eventos.'),
    cidade_assinatura = COALESCE(cidade_assinatura, 'Sao Paulo'),
    titulo_documento = COALESCE(titulo_documento, 'CONTRATO DE PRESTACAO DE SERVICOS'),
    subtitulo_documento = COALESCE(subtitulo_documento, 'Pintura ao vivo em eventos'),
    clausula_objeto = COALESCE(clausula_objeto, 'O presente contrato tem como objeto a prestacao de servico de pintura ao vivo durante o evento descrito neste documento.'),
    clausula_servico = COALESCE(clausula_servico, 'A contratada realizara a pintura ao vivo com base no briefing aprovado e nas informacoes do evento.'),
    clausula_visao_artistica = COALESCE(clausula_visao_artistica, 'A execucao da obra respeitara a linguagem artistica propria da contratada.'),
    clausula_pagamento = COALESCE(clausula_pagamento, 'O pagamento sera realizado conforme condicoes acordadas entre as partes, podendo incluir sinal e saldo final.'),
    clausula_deslocamento = COALESCE(clausula_deslocamento, 'Eventos fora da cidade base poderao incluir custos adicionais de deslocamento, hospedagem e alimentacao.'),
    clausula_cancelamento = COALESCE(clausula_cancelamento, 'Cancelamento pelo contratante: o sinal nao sera devolvido.' || CHR(10) || 'Cancelamento pela contratada: devolucao integral dos valores pagos.'),
    clausula_obrigacoes_contratada = COALESCE(clausula_obrigacoes_contratada, 'Chegar com antecedencia minima de 60 minutos.' || CHR(10) || 'Levar todos os materiais necessarios.' || CHR(10) || 'Executar o servico com postura profissional durante todo o evento.'),
    clausula_obrigacoes_contratante = COALESCE(clausula_obrigacoes_contratante, 'Disponibilizar espaco adequado para a execucao do servico.' || CHR(10) || 'Fornecer as informacoes necessarias para o briefing.' || CHR(10) || 'Efetuar os pagamentos conforme combinado.'),
    clausula_direitos_imagem = COALESCE(clausula_direitos_imagem, 'A contratada podera utilizar imagens da obra e do processo para portfolio e divulgacao, salvo oposicao expressa do contratante.'),
    clausula_disposicoes_gerais = COALESCE(clausula_disposicoes_gerais, 'Este contrato somente podera ser alterado mediante acordo escrito entre as partes.'),
    contratada_nome = COALESCE(contratada_nome, 'Atelie Exemplo'),
    contratada_documento = COALESCE(contratada_documento, '00.000.000/0001-00'),
    contratada_email = COALESCE(contratada_email, 'contato@exemplo.com'),
    contratada_telefone = COALESCE(contratada_telefone, '(11) 90000-0000');

INSERT INTO templates_contrato (
    nome, tipo_evento, caminho_arquivo, versao, descricao, cidade_assinatura, titulo_documento, subtitulo_documento,
    clausula_objeto, clausula_servico, clausula_visao_artistica, clausula_pagamento, clausula_deslocamento,
    clausula_cancelamento, clausula_obrigacoes_contratada, clausula_obrigacoes_contratante,
    clausula_direitos_imagem, clausula_disposicoes_gerais, contratada_nome, contratada_documento, contratada_email, contratada_telefone
)
SELECT
    'Template Pintura ao Vivo',
    'PINTURA_AO_VIVO',
    NULL,
    '1',
    'Modelo focado em pintura ao vivo para casamentos, aniversarios e eventos corporativos.',
    'Sao Paulo',
    'CONTRATO DE PRESTACAO DE SERVICOS',
    'Pintura ao vivo em eventos',
    'O presente contrato tem como objeto a prestacao de servico de pintura ao vivo durante o evento descrito neste documento.',
    'A contratada realizara a pintura ao vivo durante o evento conforme o briefing aprovado pelo contratante.',
    'A obra sera executada segundo o estilo artistico proprio da contratada.',
    'O pagamento sera realizado conforme as condicoes negociadas entre as partes, com possibilidade de sinal para reserva de data.',
    'Para eventos fora da cidade base, podera haver cobranca adicional de deslocamento, hospedagem e alimentacao.',
    'Cancelamento pelo contratante: o sinal nao sera devolvido.' || CHR(10) || 'Cancelamento pela contratada: devolucao integral dos valores pagos.',
    'Chegar com antecedencia minima de 60 minutos.' || CHR(10) || 'Levar todos os materiais necessarios.' || CHR(10) || 'Executar o servico com postura profissional durante todo o evento.',
    'Disponibilizar espaco adequado.' || CHR(10) || 'Compartilhar referencias e alinhamentos do evento.' || CHR(10) || 'Efetuar os pagamentos conforme combinado.',
    'A contratada podera divulgar imagens do processo e da obra, salvo restricao formal do contratante.',
    'O presente contrato passa a valer na data de sua assinatura e somente podera ser alterado por escrito.',
    'Atelie Exemplo',
    '00.000.000/0001-00',
    'contato@exemplo.com',
    '(11) 90000-0000'
WHERE NOT EXISTS (
    SELECT 1 FROM templates_contrato WHERE nome = 'Template Pintura ao Vivo'
);

INSERT INTO templates_contrato (
    nome, tipo_evento, caminho_arquivo, versao, descricao, cidade_assinatura, titulo_documento, subtitulo_documento,
    clausula_objeto, clausula_servico, clausula_visao_artistica, clausula_pagamento, clausula_deslocamento,
    clausula_cancelamento, clausula_obrigacoes_contratada, clausula_obrigacoes_contratante,
    clausula_direitos_imagem, clausula_disposicoes_gerais, contratada_nome, contratada_documento, contratada_email, contratada_telefone
)
SELECT
    'Template Pintura em Aquarela',
    'AQUARELA',
    NULL,
    '1',
    'Modelo para encomendas e experiencias de pintura em aquarela.',
    'Sao Paulo',
    'CONTRATO DE PRESTACAO DE SERVICOS',
    'Pintura artistica em aquarela',
    'O presente contrato tem como objeto a prestacao de servico artistico em aquarela conforme o escopo aprovado pelas partes.',
    'A contratada produzira a obra em aquarela com base no briefing, referencias e combinacoes formalizadas com o contratante.',
    'A obra sera elaborada de acordo com a interpretacao artistica da contratada e as caracteristicas proprias da tecnica em aquarela.',
    'O valor do servico sera pago nas condicoes ajustadas entre as partes, podendo incluir sinal para inicio da producao.',
    'Caso haja entrega presencial fora da cidade base, podera haver cobranca adicional de deslocamento e logistica.',
    'Cancelamento antes do inicio da producao: o sinal nao sera devolvido.' || CHR(10) || 'Cancelamento por impossibilidade da contratada: devolucao integral dos valores pagos.',
    'Produzir a obra respeitando a tecnica de aquarela e o briefing aprovado.' || CHR(10) || 'Informar ao contratante sobre etapas importantes da execucao.',
    'Fornecer referencias visuais e orientacoes necessarias.' || CHR(10) || 'Aprovar o briefing antes do inicio da execucao.' || CHR(10) || 'Cumprir as condicoes de pagamento acordadas.',
    'A contratada podera usar registros da obra finalizada para portfolio e divulgacao.',
    'Casos omissos serao resolvidos de comum acordo entre as partes, preservando-se a boa-fe contratual.',
    'Atelie Exemplo',
    '00.000.000/0001-00',
    'contato@exemplo.com',
    '(11) 90000-0000'
WHERE NOT EXISTS (
    SELECT 1 FROM templates_contrato WHERE nome = 'Template Pintura em Aquarela'
);
