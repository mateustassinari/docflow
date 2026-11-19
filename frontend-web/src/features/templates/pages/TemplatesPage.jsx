import { ArrowDown, ArrowUp, ChevronDown, Edit3, ExternalLink, FilePlus2, Plus, Save, Trash2 } from 'lucide-react'
import TopBar from '../../../layouts/TopBar'
import { useTemplatesPage } from '../hooks/useTemplatesPage'

const PREVIEW_CHANNEL = 'template-preview'
const PREVIEW_STORAGE_KEY = 'template-preview-payload'
const SERVICOS = [
  { value: 'PINTURA_AO_VIVO', label: 'Pintura ao Vivo' },
  { value: 'AQUARELA_AO_VIVO', label: 'Aquarela ao Vivo' },
  { value: 'ENCOMENDA_ARTISTICA', label: 'Encomenda artística personalizada' },
  { value: 'IDENTIDADE_VISUAL', label: 'Identidade visual' },
]

function serviceLabel(value) {
  return SERVICOS.find(item => item.value === value)?.label || 'Serviço geral'
}
const LIVE_SERVICE_TYPES = ['PINTURA_AO_VIVO', 'AQUARELA_AO_VIVO']
const PLACEHOLDER_TOKENS = [
  '{{cliente.nome}}',
  '{{cliente.documento}}',
  '{{cliente.email}}',
  '{{cliente.telefone}}',
  '{{evento.tipo_servico}}',
  '{{evento.tipo_servico_label}}',
  '{{evento.tipo}}',
  '{{evento.data}}',
  '{{evento.horario_inicio}}',
  '{{evento.duracao_estimada}}',
  '{{evento.local}}',
  '{{evento.numero_convidados}}',
  '{{evento.tecnica}}',
  '{{evento.tamanho_tela}}',
  '{{evento.cena_retratada}}',
  '{{evento.entrega_obra}}',
  '{{evento.valor}}',
  '{{evento.valor_sinal}}',
  '{{evento.pagamento_sinal}}',
  '{{evento.pagamento_restante}}',
  '{{evento.observacoes}}',
  '{{dadosServico.tipoProduto}}',
  '{{dadosServico.dimensao}}',
  '{{dadosServico.tecnica}}',
  '{{dadosServico.temaReferencia}}',
  '{{dadosServico.prazoEstimado}}',
  '{{dadosServico.formaEntrega}}',
  '{{dadosServico.enderecoEnvio}}',
  '{{dadosServico.freteResponsavel}}',
  '{{dadosServico.prazoDiasUteis}}',
  '{{dadosServico.rodadasAjustes}}',
  '{{template.cidade_assinatura}}',
  '{{template.cidade_data}}',
  '{{contratada.nome}}',
  '{{contratada.documento}}',
  '{{contratada.email}}',
  '{{contratada.telefone}}',
]
const SUGGESTED_CLAUSES = {
  PINTURA_AO_VIVO: [
    {
      titulo: 'CLÁUSULA 1 - OBJETO',
      texto: 'O presente contrato tem como objeto a prestação do serviço de {{evento.tipo_servico_label}} durante o evento {{evento.tipo}}, a ser realizado em {{evento.data}}, no local {{evento.local}}.',
      itens: ['Cena retratada: {{evento.cena_retratada}}', 'Técnica: {{evento.tecnica}}', 'Entrega da obra: {{evento.entrega_obra}}'],
    },
    {
      titulo: 'CLÁUSULA 2 - VALOR E PAGAMENTO',
      texto: 'O valor total do serviço é {{evento.valor}}, com sinal de {{evento.valor_sinal}}. O sinal será pago por {{evento.pagamento_sinal}} e o restante por {{evento.pagamento_restante}}.',
      itens: [],
    },
  ],
  AQUARELA_AO_VIVO: [
    {
      titulo: 'CLÁUSULA 1 - OBJETO',
      texto: 'O presente contrato tem como objeto a prestação do serviço de {{evento.tipo_servico_label}}, com produção artística em aquarela durante o evento {{evento.tipo}}.',
      itens: ['Técnica: {{evento.tecnica}}', 'Duração estimada: {{evento.duracao_estimada}}', 'Entrega da obra: {{evento.entrega_obra}}'],
    },
    {
      titulo: 'CLÁUSULA 2 - VALOR E PAGAMENTO',
      texto: 'O valor total do serviço é {{evento.valor}}, com sinal de {{evento.valor_sinal}}. O pagamento seguirá as condições acordadas entre as partes.',
      itens: [],
    },
  ],
  ENCOMENDA_ARTISTICA: [
    {
      titulo: 'CLÁUSULA 1 - OBJETO',
      texto: 'O presente contrato tem como objeto a criação de encomenda artística personalizada, conforme produto e referências aprovadas pelas partes.',
      itens: ['Produto contratado: {{dadosServico.tipoProduto}}', 'Dimensão: {{dadosServico.dimensao}}', 'Prazo estimado: {{dadosServico.prazoEstimado}}'],
    },
    {
      titulo: 'CLÁUSULA 2 - ENVIO E ENTREGA',
      texto: 'A entrega/envio da obra seguirá o endereço e as condições definidos nos dados da encomenda.',
      itens: ['Endereço/envio: {{dadosServico.enderecoEnvio}}', 'Frete/responsável: {{dadosServico.freteResponsavel}}'],
    },
  ],
  IDENTIDADE_VISUAL: [
    {
      titulo: 'CLÁUSULA 1 - OBJETO E ESCOPO',
      texto: 'O presente contrato tem como objeto o desenvolvimento de identidade visual, conforme escopo aprovado entre as partes.',
      itens: ['Prazo em dias úteis: {{dadosServico.prazoDiasUteis}}', 'Rodadas de ajustes: {{dadosServico.rodadasAjustes}}'],
    },
    {
      titulo: 'CLÁUSULA 2 - DIREITOS DE USO',
      texto: 'A contratante poderá utilizar a identidade visual aprovada após a quitação integral dos valores contratados.',
      itens: [],
    },
  ],
}

const EMPTY_TEMPLATE = {
  nome: '',
  tipoServico: 'PINTURA_AO_VIVO',
  tipoEvento: 'GERAL',
  descricao: '',
  versao: '1',
  cidadeAssinatura: 'São Paulo',
  tituloDocumento: 'CONTRATO DE PRESTAÇÃO DE SERVIÇOS',
  subtituloDocumento: '',
  clausulaObjeto: '',
  clausulaServico: '',
  clausulaVisaoArtistica: '',
  clausulaPagamento: '',
  clausulaDeslocamento: '',
  clausulaCancelamentoContratada: '',
  clausulaCancelamentoContratante: '',
  clausulaObrigacoesContratada: '',
  clausulaObrigacoesContratante: '',
  clausulaDireitosImagem: '',
  clausulaDisposicoesGerais: '',
  clausulasConfig: '',
  contratadaNome: '',
  contratadaDocumento: '',
  contratadaEmail: '',
  contratadaTelefone: '',
}

const PREVIEW_PLACEHOLDERS = {
  clienteNome: 'Ana Paula Martins',
  clienteCpfCnpj: '123.456.789-00',
  clienteEmail: 'ana@email.com',
  clienteTelefone: '(11) 99999-1234',
  eventoTipo: 'Casamento',
  eventoData: '2026-10-18',
  eventoHorarioInicio: '17:30',
  eventoDuracaoEstimada: '5 horas',
  eventoLocal: 'Vila Maria Eventos',
  eventoNumeroConvidados: 180,
  eventoTecnica: 'Acrílica sobre tela',
  eventoTamanhoTela: '80x100 cm',
  eventoCenaRetratada: 'Primeira dança dos noivos',
  eventoEntregaObra: 'Ao final do evento',
  eventoValor: 6800,
  eventoValorSinal: 2500,
  eventoFormaPagamentoSinal: 'Pix no ato da assinatura',
  eventoFormaPagamentoRestante: 'Pix até a data do evento',
  eventoObservacoes: 'Montagem prevista com 1h de antecedência.',
}

const PREVIEW_SERVICE_DATA = {
  PINTURA_AO_VIVO: {
    horarioInicio: '17:30',
    duracaoEstimada: '5 horas',
    numeroConvidados: '180',
    tecnica: 'Acrílica sobre tela',
    tamanhoTela: '80x100 cm',
    cenaRetratada: 'Primeira dança dos noivos',
    entregaObra: 'Ao final do evento',
  },
  AQUARELA_AO_VIVO: {
    horarioInicio: '16:00',
    duracaoEstimada: '4 horas',
    numeroConvidados: '120',
    tecnica: 'Aquarela sobre papel artístico',
    tamanhoTela: 'A3',
    cenaRetratada: 'Cerimônia e recepção dos convidados',
    entregaObra: 'Em até 15 dias após o evento',
  },
  ENCOMENDA_ARTISTICA: {
    tipoProduto: 'Porta aliança',
    dimensao: '18x18 cm',
    tecnica: 'Acrílica e acabamento artesanal',
    temaReferencia: 'Flores do convite e paleta verde oliva',
    prazoEstimado: '30 dias',
    formaEntrega: 'ENVIO',
    enderecoEnvio: 'Rua das Flores, 123 - São Paulo/SP',
    freteResponsavel: 'Frete por conta do contratante',
  },
  IDENTIDADE_VISUAL: {
    prazoDiasUteis: '30',
    rodadasAjustes: '2',
  },
}

function buildPreviewPayload(form) {
  const serviceData = PREVIEW_SERVICE_DATA[form.tipoServico] || PREVIEW_SERVICE_DATA.PINTURA_AO_VIVO

  return {
    ...form,
    ...PREVIEW_PLACEHOLDERS,
    eventoTipoServico: form.tipoServico,
    eventoDadosServico: JSON.stringify(serviceData),
    eventoHorarioInicio: serviceData.horarioInicio || PREVIEW_PLACEHOLDERS.eventoHorarioInicio,
    eventoDuracaoEstimada: serviceData.duracaoEstimada || PREVIEW_PLACEHOLDERS.eventoDuracaoEstimada,
    eventoNumeroConvidados: Number(serviceData.numeroConvidados || PREVIEW_PLACEHOLDERS.eventoNumeroConvidados),
    eventoTecnica: serviceData.tecnica || PREVIEW_PLACEHOLDERS.eventoTecnica,
    eventoTamanhoTela: serviceData.tamanhoTela || PREVIEW_PLACEHOLDERS.eventoTamanhoTela,
    eventoCenaRetratada: serviceData.cenaRetratada || PREVIEW_PLACEHOLDERS.eventoCenaRetratada,
    eventoEntregaObra: serviceData.entregaObra || PREVIEW_PLACEHOLDERS.eventoEntregaObra,
  }
}

function publishPreviewPayload(payload) {
  localStorage.setItem(PREVIEW_STORAGE_KEY, JSON.stringify(payload))

  if ('BroadcastChannel' in window) {
    const channel = new BroadcastChannel(PREVIEW_CHANNEL)
    channel.postMessage(payload)
    channel.close()
  }
}

function parseDynamicClauses(value) {
  if (!value || !value.trim()) return { clauses: [], invalid: false }

  try {
    const parsed = JSON.parse(value)
    if (!Array.isArray(parsed)) return { clauses: [], invalid: true }

    return {
      invalid: false,
      clauses: parsed.map((clause, index) => ({
        titulo: clause?.titulo || `CLÁUSULA ${index + 1}`,
        textoAntes: clause?.textoAntes ?? clause?.texto ?? '',
        textoDepois: clause?.textoDepois ?? '',
        itens: Array.isArray(clause?.itens) ? clause.itens.map(String) : [],
      })),
    }
  } catch {
    return { clauses: [], invalid: true }
  }
}

function stringifyDynamicClauses(clauses) {
  const cleaned = clauses
    .map(clause => ({
      titulo: clause.titulo || '',
      textoAntes: clause.textoAntes ?? clause.texto ?? '',
      itens: clause.itens || [],
      textoDepois: clause.textoDepois || '',
    }))
    .filter(clause => clause.titulo.trim() || clause.textoAntes.trim() || clause.itens.length > 0 || clause.textoDepois.trim())

  return cleaned.length > 0 ? JSON.stringify(cleaned, null, 2) : ''
}

export default function Templates() {
  const {
    dynamicClauses,
    dynamicClausesState,
    form,
    handleAddClause,
    handleAddClauseItem,
    handleChange,
    handleMoveClause,
    handleNew,
    handleOpenPreviewWindow,
    handleRemoveClause,
    handleRemoveClauseItem,
    handleSelect,
    handleSubmit,
    handleUpdateClause,
    handleUpdateClauseItem,
    handleUseSuggestedClauses,
    isCreatingVersion,
    loading,
    openEditorCards,
    saving,
    selectedId,
    selectedTemplate,
    selectedVersion,
    showDynamicClauseSections,
    showPaintClauseSections,
    templates,
    toast,
    toggleEditorCard,
  } = useTemplatesPage({
    emptyTemplate: EMPTY_TEMPLATE,
    buildPreviewPayload,
    parseDynamicClauses,
    publishPreviewPayload,
    stringifyDynamicClauses,
    normalizeTemplate,
    suggestedClauses: SUGGESTED_CLAUSES,
    liveServiceTypes: LIVE_SERVICE_TYPES,
  })

  return (
    <>
      <TopBar breadcrumb={[{ label: 'Templates' }]} title="Templates" />
      <div className="page-content">
        <div className="page-header">
          <div className="page-header-left">
            <h1>Modelos de contrato em PDF</h1>
            <p>O painel da direita renderiza o PDF real gerado pelo backend, com os mesmos estilos e regras do OpenPDF.</p>
          </div>
          <button className="btn btn-primary" onClick={handleNew}>
            <FilePlus2 size={16} />
            Novo template
          </button>
        </div>

        {loading ? (
          <div className="card">Carregando templates...</div>
        ) : (
          <div className="template-studio">
            <aside className="template-sidebar-panel">
              <div className="template-sidebar-block">
                <div className="template-sidebar-title">Selecione um Template</div>
                {!selectedTemplate && (
                  <div className="template-create-state">
                    <FilePlus2 size={16} />
                    <span>
                      <strong>Novo template em criação</strong>
                      Preencha a identidade e salve para adicionar na lista.
                    </span>
                  </div>
                )}
                <div className="template-selector-list">
                  {templates.length === 0 ? (
                    <p className="template-empty-note">Nenhum template cadastrado ainda.</p>
                  ) : (
                    templates.map(template => {
                      const active = template.id === selectedId
                      return (
                        <button
                          key={template.id}
                          type="button"
                          className={`template-selector-card ${active ? 'active' : ''}`}
                          onClick={() => handleSelect(template)}
                        >
                          <div className="template-selector-icon">
                            <Edit3 size={16} />
                          </div>
                          <div className="template-selector-text">
                            <strong>{template.nome}</strong>
                            <span>{template.descricao || `${serviceLabel(template.tipoServico)} · v${template.versao || '1'}`}</span>
                          </div>
                        </button>
                      )
                    })
                  )}
                </div>
              </div>
            </aside>

            <form className="template-main-panel" onSubmit={handleSubmit}>
              <div className="template-preview-launcher">
                <div>
                  <strong>Preview do PDF em janela separada</strong>
                  <span>Abra a visualização e deixe ao lado enquanto edita. Ela atualiza automaticamente.</span>
                </div>
                <button className="btn btn-primary" type="button" onClick={handleOpenPreviewWindow}>
                  <ExternalLink size={14} />
                  Abrir preview em janela
                </button>
              </div>

              <div className="template-editor-grid">
                {!selectedTemplate && (
                  <div className="template-new-banner">
                    <FilePlus2 size={18} />
                    <div>
                      <strong>Criando novo template</strong>
                      <span>Este modelo ainda não foi salvo. Comece pela identidade do documento e revise as seções que quiser editar.</span>
                    </div>
                  </div>
                )}

                <EditorCard title="Identidade do documento" open={Boolean(openEditorCards.identidade)} onToggle={() => toggleEditorCard('identidade')}>
                  <div className="form-grid-2">
                    <Field label="Nome">
                      <input className="form-input" value={form.nome} onChange={e => handleChange('nome', e.target.value)} required />
                    </Field>
                    <Field label="Tipo de serviço">
                      <select className="form-select" value={form.tipoServico} onChange={e => handleChange('tipoServico', e.target.value)}>
                        {SERVICOS.map(opcaoServico => <option key={opcaoServico.value} value={opcaoServico.value}>{opcaoServico.label}</option>)}
                      </select>
                    </Field>
                    <Field label="Versão">
                      <input className="form-input" value={form.versao} onChange={e => handleChange('versao', e.target.value)} />
                    </Field>
                    <Field label="Título do documento">
                      <input className="form-input" value={form.tituloDocumento} onChange={e => handleChange('tituloDocumento', e.target.value)} />
                    </Field>
                    <Field label="Subtítulo do documento">
                      <input className="form-input" value={form.subtituloDocumento} onChange={e => handleChange('subtituloDocumento', e.target.value)} />
                    </Field>
                  </div>
                  <Field label="Descrição">
                    <textarea className="form-textarea" rows={3} value={form.descricao} onChange={e => handleChange('descricao', e.target.value)} />
                  </Field>
                  <Field label="Cidade da assinatura">
                    <input className="form-input" value={form.cidadeAssinatura} onChange={e => handleChange('cidadeAssinatura', e.target.value)} />
                  </Field>
                </EditorCard>

                {showDynamicClauseSections && (
                  <EditorCard title="Cláusulas do contrato" open={Boolean(openEditorCards.principais)} onToggle={() => toggleEditorCard('principais')}>
                    <div className="placeholder-token-panel">
                      <strong className="placeholder-token-title">Placeholders dinâmicos</strong>
                      <p className="placeholder-token-description">
                        Use estes tokens nas cláusulas, no título ou no subtítulo para personalizar o PDF por serviço e por evento.
                      </p>
                      <div className="placeholder-token-list">
                        {PLACEHOLDER_TOKENS.map(token => (
                          <code key={token} className="placeholder-token">
                            {token}
                          </code>
                        ))}
                      </div>
                    </div>

                    {dynamicClausesState.invalid && (
                      <div className="event-edit-warning template-warning-spaced">
                        As cláusulas salvas estão em um formato antigo ou inválido. Clique em "Usar modelo sugerido" para recriar a estrutura editável.
                      </div>
                    )}

                    <div className="dynamic-clause-actions">
                      <button type="button" className="btn btn-secondary" onClick={handleUseSuggestedClauses}>
                        <FilePlus2 size={14} />
                        Usar modelo sugerido
                      </button>
                      <button type="button" className="btn btn-secondary" onClick={handleAddClause}>
                        <Plus size={14} />
                        Adicionar cláusula
                      </button>
                    </div>

                    {dynamicClauses.length === 0 ? (
                      <div className="empty-state dynamic-clause-empty">
                        <FilePlus2 size={32} className="icon-soft" />
                        <h3>Nenhuma cláusula dinâmica ainda</h3>
                        <p>Clique em "Usar modelo sugerido" para começar com uma estrutura pronta para este tipo de serviço.</p>
                      </div>
                    ) : (
                      <div className="dynamic-clause-list">
                        {dynamicClauses.map((clause, clauseIndex) => (
                          <div key={clauseIndex} className="dynamic-clause-card">
                            <div className="dynamic-clause-header">
                              <strong className="dynamic-clause-title">Cláusula {clauseIndex + 1}</strong>
                              <button type="button" className="topbar-icon-btn" onClick={() => handleMoveClause(clauseIndex, -1)} disabled={clauseIndex === 0} title="Mover para cima">
                                <ArrowUp size={14} />
                              </button>
                              <button type="button" className="topbar-icon-btn" onClick={() => handleMoveClause(clauseIndex, 1)} disabled={clauseIndex === dynamicClauses.length - 1} title="Mover para baixo">
                                <ArrowDown size={14} />
                              </button>
                              <button type="button" className="topbar-icon-btn" onClick={() => handleRemoveClause(clauseIndex)} title="Remover cláusula">
                                <Trash2 size={14} />
                              </button>
                            </div>

                            <Field label="Título da cláusula">
                              <input className="form-input" value={clause.titulo} onChange={e => handleUpdateClause(clauseIndex, 'titulo', e.target.value)} placeholder="Ex: CLÁUSULA 1 - OBJETO" />
                            </Field>
                            <Field label="Texto antes dos itens">
                              <textarea className="form-textarea" rows={4} value={clause.textoAntes || ''} onChange={e => handleUpdateClause(clauseIndex, 'textoAntes', e.target.value)} placeholder="Ex: O projeto contempla os seguintes itens:" />
                            </Field>

                            <div className="dynamic-clause-items">
                              <div className="form-label dynamic-clause-items-label">Itens opcionais</div>
                              {(clause.itens || []).map((item, itemIndex) => (
                                <div key={itemIndex} className="dynamic-clause-item-row">
                                  <input className="form-input" value={item} onChange={e => handleUpdateClauseItem(clauseIndex, itemIndex, e.target.value)} placeholder="Ex: Produto contratado: {{dadosServico.tipoProduto}}" />
                                  <button type="button" className="topbar-icon-btn" onClick={() => handleRemoveClauseItem(clauseIndex, itemIndex)} title="Remover item">
                                    <Trash2 size={14} />
                                  </button>
                                </div>
                              ))}
                              <button type="button" className="btn btn-secondary" onClick={() => handleAddClauseItem(clauseIndex)}>
                                <Plus size={14} />
                                Adicionar item
                              </button>
                            </div>

                            <Field label="Texto depois dos itens">
                              <textarea className="form-textarea" rows={4} value={clause.textoDepois || ''} onChange={e => handleUpdateClause(clauseIndex, 'textoDepois', e.target.value)} placeholder="Ex: Itens adicionais ou alterações de escopo serão orçados separadamente." />
                            </Field>
                          </div>
                        ))}
                      </div>
                    )}
                  </EditorCard>
                )}

                {showPaintClauseSections && (
                  <>
                    <EditorCard title="Cláusulas principais" open={Boolean(openEditorCards.pinturas)} onToggle={() => toggleEditorCard('pinturas')}>
                      <Field label="Cláusula de objeto">
                        <textarea className="form-textarea" rows={4} value={form.clausulaObjeto} onChange={e => handleChange('clausulaObjeto', e.target.value)} />
                      </Field>
                      <Field label="Cláusula de serviço">
                        <textarea className="form-textarea" rows={4} value={form.clausulaServico} onChange={e => handleChange('clausulaServico', e.target.value)} />
                      </Field>
                      <Field label="Cláusula de visão artística">
                        <textarea className="form-textarea" rows={4} value={form.clausulaVisaoArtistica} onChange={e => handleChange('clausulaVisaoArtistica', e.target.value)} />
                      </Field>
                      <Field label="Cláusula de pagamento">
                        <textarea className="form-textarea" rows={4} value={form.clausulaPagamento} onChange={e => handleChange('clausulaPagamento', e.target.value)} />
                      </Field>
                      <Field label="Cláusula de deslocamento">
                        <textarea className="form-textarea" rows={4} value={form.clausulaDeslocamento} onChange={e => handleChange('clausulaDeslocamento', e.target.value)} />
                      </Field>
                    </EditorCard>

                    <EditorCard title="Listas e regras" open={Boolean(openEditorCards.listas)} onToggle={() => toggleEditorCard('listas')}>
                      <Field label="Cancelamento pela contratada">
                        <textarea className="form-textarea" rows={4} value={form.clausulaCancelamentoContratada} onChange={e => handleChange('clausulaCancelamentoContratada', e.target.value)} />
                      </Field>
                      <Field label="Cancelamento pelo contratante">
                        <textarea className="form-textarea" rows={4} value={form.clausulaCancelamentoContratante} onChange={e => handleChange('clausulaCancelamentoContratante', e.target.value)} />
                      </Field>
                      <Field label="Obrigações da contratada">
                        <textarea className="form-textarea" rows={4} value={form.clausulaObrigacoesContratada} onChange={e => handleChange('clausulaObrigacoesContratada', e.target.value)} />
                      </Field>
                      <Field label="Obrigações do contratante">
                        <textarea className="form-textarea" rows={4} value={form.clausulaObrigacoesContratante} onChange={e => handleChange('clausulaObrigacoesContratante', e.target.value)} />
                      </Field>
                    </EditorCard>

                    <EditorCard title="Cláusulas finais" open={Boolean(openEditorCards.finais)} onToggle={() => toggleEditorCard('finais')}>
                      <Field label="Direitos de imagem">
                        <textarea className="form-textarea" rows={4} value={form.clausulaDireitosImagem} onChange={e => handleChange('clausulaDireitosImagem', e.target.value)} />
                      </Field>
                      <Field label="Disposições gerais">
                        <textarea className="form-textarea" rows={4} value={form.clausulaDisposicoesGerais} onChange={e => handleChange('clausulaDisposicoesGerais', e.target.value)} />
                      </Field>
                    </EditorCard>
                  </>
                )}

                <EditorCard title="Dados da contratada" open={Boolean(openEditorCards.contratada)} onToggle={() => toggleEditorCard('contratada')}>
                  <div className="form-grid-2">
                    <Field label="Nome">
                      <input className="form-input" value={form.contratadaNome} onChange={e => handleChange('contratadaNome', e.target.value)} />
                    </Field>
                    <Field label="Documento">
                      <input className="form-input" value={form.contratadaDocumento} onChange={e => handleChange('contratadaDocumento', e.target.value)} />
                    </Field>
                    <Field label="E-mail">
                      <input className="form-input" value={form.contratadaEmail} onChange={e => handleChange('contratadaEmail', e.target.value)} />
                    </Field>
                    <Field label="Telefone">
                      <input className="form-input" value={form.contratadaTelefone} onChange={e => handleChange('contratadaTelefone', e.target.value)} />
                    </Field>
                  </div>
                </EditorCard>
              </div>

              <div className="template-savebar">
                <div className="template-savebar-note">
                  {isCreatingVersion
                    ? `A versão ${form.versao || '-'} será salva como novo template. A versão ${selectedVersion} será preservada.`
                    : selectedTemplate ? `Editando template #${selectedTemplate.id}` : 'Novo template ainda não salvo'}
                </div>
                <button className="btn btn-primary" type="submit" disabled={saving}>
                  <Save size={16} />
                  {saving ? 'Salvando...' : isCreatingVersion ? 'Criar nova versão' : 'Salvar template'}
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
      {toast && <div className={`toast ${toast.type}`}>{toast.msg}</div>}
    </>
  )
}

function Field({ label, children }) {
  return (
    <label className="form-group">
      <span className="form-label">{label}</span>
      {children}
    </label>
  )
}

function EditorCard({ title, children, open, onToggle }) {
  return (
    <section className={`template-editor-card ${open ? 'open' : ''}`}>
      <button type="button" className="template-editor-card-toggle" onClick={onToggle} aria-expanded={open}>
        <h2>{title}</h2>
        <span>
          {open ? 'Ocultar' : 'Editar'}
          <ChevronDown size={16} />
        </span>
      </button>
      {open && <div className="template-editor-card-body">{children}</div>}
    </section>
  )
}

function normalizeTemplate(template) {
  const normalized = {
    ...EMPTY_TEMPLATE,
    ...template,
  }

  Object.keys(EMPTY_TEMPLATE).forEach(key => {
    if (normalized[key] == null) {
      normalized[key] = EMPTY_TEMPLATE[key] ?? ''
    }
  })

  return normalized
}
