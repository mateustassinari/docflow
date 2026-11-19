ALTER TABLE events
    ADD COLUMN IF NOT EXISTS tipo_servico VARCHAR(80),
    ADD COLUMN IF NOT EXISTS dados_servico TEXT;

ALTER TABLE templates_contrato
    ADD COLUMN IF NOT EXISTS tipo_servico VARCHAR(80),
    ADD COLUMN IF NOT EXISTS clausulas_config TEXT;

UPDATE templates_contrato
SET tipo_servico = COALESCE(tipo_servico, CASE
    WHEN LOWER(nome) LIKE '%aquarela%' THEN 'AQUARELA_AO_VIVO'
    WHEN LOWER(nome) LIKE '%encomenda%' THEN 'ENCOMENDA_ARTISTICA'
    WHEN LOWER(nome) LIKE '%identidade%' THEN 'IDENTIDADE_VISUAL'
    WHEN LOWER(nome) LIKE '%pintura ao vivo%' THEN 'PINTURA_AO_VIVO'
    ELSE NULL
END);

INSERT INTO templates_contrato (
    nome, tipo_servico, tipo_evento, caminho_arquivo, versao, descricao, cidade_assinatura,
    titulo_documento, subtitulo_documento, clausulas_config, contratada_nome, contratada_documento,
    contratada_email, contratada_telefone
)
SELECT
    'Template Aquarela ao Vivo',
    'AQUARELA_AO_VIVO',
    'GERAL',
    'generated/aquarela-ao-vivo.pdf',
    '1',
    'Template inicial para aquarela ao vivo com fluxo de pintura ao vivo.',
    'Sao Paulo',
    'CONTRATO DE PRESTACAO DE SERVICOS',
    'Aquarela ao vivo em eventos',
    '[{"titulo":"CLAUSULA 1 - OBJETO","texto":"O presente contrato tem como objeto a prestacao do servico de {{evento.tipo_servico_label}} durante o evento {{evento.tipo}}, conforme os dados informados pelas partes."},{"titulo":"CLAUSULA 2 - SERVICO PRESTADO","texto":"A contratada realizara a pintura em aquarela ao vivo, utilizando tecnica artistica propria e referencias combinadas previamente."},{"titulo":"CLAUSULA 3 - VALOR E PAGAMENTO","texto":"O valor total do servico e {{evento.valor}}, com sinal de {{evento.valor_sinal}}. O sinal sera pago por {{evento.pagamento_sinal}} e o restante por {{evento.pagamento_restante}}."},{"titulo":"CLAUSULA 4 - DIREITOS DE IMAGEM","texto":"A contratada podera utilizar imagens do processo e da obra para portfolio e divulgacao, salvo restricao expressa do contratante."}]',
    'Atelie Exemplo',
    '00.000.000/0001-00',
    'contato@exemplo.com',
    '(11) 90000-0000'
WHERE NOT EXISTS (SELECT 1 FROM templates_contrato WHERE tipo_servico = 'AQUARELA_AO_VIVO');

INSERT INTO templates_contrato (
    nome, tipo_servico, tipo_evento, caminho_arquivo, versao, descricao, cidade_assinatura,
    titulo_documento, subtitulo_documento, clausulas_config, contratada_nome, contratada_documento,
    contratada_email, contratada_telefone
)
SELECT
    'Template Encomenda Artistica',
    'ENCOMENDA_ARTISTICA',
    'GERAL',
    'generated/encomenda-artistica.pdf',
    '1',
    'Template inicial para encomendas artisticas, incluindo porta alianca.',
    'Sao Paulo',
    'CONTRATO DE ENCOMENDA ARTISTICA PERSONALIZADA',
    'Produto contratado: {{dadosServico.tipoProduto}}',
    '[{"titulo":"CLAUSULA 1 - OBJETO","texto":"O presente contrato tem como objeto a producao de encomenda artistica personalizada. Produtos possiveis: pintura artistica, bordado artistico, retrato personalizado, peca decorativa artesanal, papelaria artistica e porta alianca.","itens":["Produto contratado: {{dadosServico.tipoProduto}}","Detalhe do produto: {{dadosServico.tipoProdutoDetalhe}}","Dimensao: {{dadosServico.dimensao}}","Tecnica: {{dadosServico.tecnica}}"]},{"titulo":"CLAUSULA 2 - REFERENCIAS E PRAZO","texto":"A obra sera criada a partir do tema/referencia informado: {{dadosServico.temaReferencia}}. Prazo estimado: {{dadosServico.prazoEstimado}}."},{"titulo":"CLAUSULA 3 - ENVIO","texto":"Endereco/envio: {{dadosServico.enderecoEnvio}}. Frete/responsavel pelo envio: {{dadosServico.freteResponsavel}}."},{"titulo":"CLAUSULA 4 - PAGAMENTO","texto":"O valor total e {{evento.valor}}, com sinal de {{evento.valor_sinal}}."}]',
    'Atelie Exemplo',
    '00.000.000/0001-00',
    'contato@exemplo.com',
    '(11) 90000-0000'
WHERE NOT EXISTS (SELECT 1 FROM templates_contrato WHERE tipo_servico = 'ENCOMENDA_ARTISTICA');

INSERT INTO templates_contrato (
    nome, tipo_servico, tipo_evento, caminho_arquivo, versao, descricao, cidade_assinatura,
    titulo_documento, subtitulo_documento, clausulas_config, contratada_nome, contratada_documento,
    contratada_email, contratada_telefone
)
SELECT
    'Template Identidade Visual',
    'IDENTIDADE_VISUAL',
    'GERAL',
    'generated/identidade-visual.pdf',
    '1',
    'Template inicial para identidade visual.',
    'Sao Paulo',
    'CONTRATO DE IDENTIDADE VISUAL',
    'Projeto de identidade visual',
    '[{"titulo":"CLAUSULA 1 - ESCOPO","texto":"O projeto contempla os seguintes itens: {{dadosServico.escopoItens}}."},{"titulo":"CLAUSULA 2 - PRAZO E AJUSTES","texto":"O prazo estimado e de {{dadosServico.prazoDiasUteis}} dias uteis. Estao inclusas {{dadosServico.rodadasAjustes}} rodadas de ajustes."},{"titulo":"CLAUSULA 3 - ALTERACOES","texto":"Regras de alteracao: {{dadosServico.regrasAlteracao}}."},{"titulo":"CLAUSULA 4 - IMPRESSAO E FORNECEDORES","texto":"Observacoes sobre impressao/fornecedores: {{dadosServico.observacoesImpressao}}."},{"titulo":"CLAUSULA 5 - DIREITOS DE USO","texto":"A contratante podera utilizar a identidade visual aprovada apos a quitacao integral dos valores contratados."}]',
    'Atelie Exemplo',
    '00.000.000/0001-00',
    'contato@exemplo.com',
    '(11) 90000-0000'
WHERE NOT EXISTS (SELECT 1 FROM templates_contrato WHERE tipo_servico = 'IDENTIDADE_VISUAL');
