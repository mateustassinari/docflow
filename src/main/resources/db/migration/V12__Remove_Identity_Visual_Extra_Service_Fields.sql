UPDATE templates_contrato
SET clausulas_config = REPLACE(
    REPLACE(
        REPLACE(
            REPLACE(
                REPLACE(
                    REPLACE(
                        REPLACE(
                            clausulas_config,
                            '{"titulo":"CLAUSULA 1 - ESCOPO","texto":"O projeto contempla os seguintes itens: {{dadosServico.escopoItens}}."},',
                            ''
                        ),
                        '{"titulo":"CLAUSULA 3 - ALTERACOES","texto":"Regras de alteracao: {{dadosServico.regrasAlteracao}}."},',
                        ''
                    ),
                    '{"titulo":"CLAUSULA 4 - IMPRESSAO E FORNECEDORES","texto":"Observacoes sobre impressao/fornecedores: {{dadosServico.observacoesImpressao}}."},',
                    ''
                ),
                'Escopo/itens inclusos: {{dadosServico.escopoItens}},',
                ''
            ),
            'Regras de alteracao: {{dadosServico.regrasAlteracao}},',
            ''
        ),
        'Impressao/fornecedores: {{dadosServico.observacoesImpressao}},',
        ''
    ),
    '{{dadosServico.escopoItens}}',
    ''
)
WHERE clausulas_config LIKE '%dadosServico.escopoItens%'
   OR clausulas_config LIKE '%dadosServico.regrasAlteracao%'
   OR clausulas_config LIKE '%dadosServico.observacoesImpressao%';

UPDATE templates_contrato
SET clausulas_config = REPLACE(
    REPLACE(
        clausulas_config,
        '{{dadosServico.regrasAlteracao}}',
        ''
    ),
    '{{dadosServico.observacoesImpressao}}',
    ''
)
WHERE clausulas_config LIKE '%dadosServico.regrasAlteracao%'
   OR clausulas_config LIKE '%dadosServico.observacoesImpressao%';

UPDATE events
SET dados_servico = REGEXP_REPLACE(
    REGEXP_REPLACE(
        REGEXP_REPLACE(
            REGEXP_REPLACE(
                REGEXP_REPLACE(
                    REGEXP_REPLACE(
                        REGEXP_REPLACE(
                            REGEXP_REPLACE(
                                REGEXP_REPLACE(
                                    dados_servico,
                                    '"escopoItens"\s*:\s*"[^"]*",',
                                    '',
                                    'g'
                                ),
                                ',\s*"escopoItens"\s*:\s*"[^"]*"',
                                '',
                                'g'
                            ),
                            '"escopoItens"\s*:\s*"[^"]*"',
                            '',
                            'g'
                        ),
                        '"regrasAlteracao"\s*:\s*"[^"]*",',
                        '',
                        'g'
                    ),
                    ',\s*"regrasAlteracao"\s*:\s*"[^"]*"',
                    '',
                    'g'
                ),
                '"regrasAlteracao"\s*:\s*"[^"]*"',
                '',
                'g'
            ),
            '"observacoesImpressao"\s*:\s*"[^"]*",',
            '',
            'g'
        ),
        ',\s*"observacoesImpressao"\s*:\s*"[^"]*"',
        '',
        'g'
    ),
    '"observacoesImpressao"\s*:\s*"[^"]*"',
    '',
    'g'
)
WHERE dados_servico LIKE '%escopoItens%'
   OR dados_servico LIKE '%regrasAlteracao%'
   OR dados_servico LIKE '%observacoesImpressao%';
