UPDATE templates_contrato
SET clausulas_config = REPLACE(
    REPLACE(
        REPLACE(
            REPLACE(
                clausulas_config,
                ',"Detalhe do produto: {{dadosServico.tipoProdutoDetalhe}}"',
                ''
            ),
            '"Detalhe do produto: {{dadosServico.tipoProdutoDetalhe}}",',
            ''
        ),
        '"Detalhe do produto: {{dadosServico.tipoProdutoDetalhe}}"',
        ''
    ),
    '{{dadosServico.tipoProdutoDetalhe}}',
    ''
)
WHERE clausulas_config LIKE '%dadosServico.tipoProdutoDetalhe%';

UPDATE events
SET dados_servico = REGEXP_REPLACE(
    REGEXP_REPLACE(
        REGEXP_REPLACE(
            dados_servico,
            '"tipoProdutoDetalhe"\s*:\s*"[^"]*",',
            '',
            'g'
        ),
        ',\s*"tipoProdutoDetalhe"\s*:\s*"[^"]*"',
        '',
        'g'
    ),
    '"tipoProdutoDetalhe"\s*:\s*"[^"]*"',
    '',
    'g'
)
WHERE dados_servico LIKE '%tipoProdutoDetalhe%';
