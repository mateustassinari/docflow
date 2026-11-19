import React from 'react'
import { useParams, useNavigate, useSearchParams } from 'react-router-dom'
import TopBar from '../../../layouts/TopBar'
import CurrencyInput from '../../../shared/components/CurrencyInput'
import ConfirmDialog from '../../../shared/components/ConfirmDialog'
import { formatDateBR } from '../../../utils/date'
import { formatCurrencyInput, parseCurrency } from '../../../utils/masks'
import { useEventDetailPage } from '../hooks/useEventDetailPage'
import { contractsService } from '../../contracts/services/contractsService'
import { financialService } from '../../financial/services/financialService'
import TaskModal from '../../tasks/components/TaskModal'
import { useTasksPage } from '../../tasks/hooks/useTasksPage'
import {
  Pencil,
  CheckCircle2,
  Send,
  PenLine,
  MapPin,
  FileText,
  ExternalLink,
  Save,
  AlertTriangle,
  CalendarPlus,
  CheckSquare,
  Square,
  X
} from 'lucide-react'

const TIPOS = ['Corporativo', 'Casamento', 'Social', 'Conferência', 'Workshop', 'Jantar Executivo']
const SERVICOS = [
  { value: 'PINTURA_AO_VIVO', label: 'Pintura ao Vivo' },
  { value: 'AQUARELA_AO_VIVO', label: 'Aquarela ao Vivo' },
  { value: 'ENCOMENDA_ARTISTICA', label: 'Encomenda artística personalizada' },
  { value: 'IDENTIDADE_VISUAL', label: 'Identidade visual' },
]
const PRODUTOS_ENCOMENDA = ['Pintura artística', 'Bordado artístico', 'Retrato personalizado', 'Peça decorativa artesanal', 'Papelaria artística', 'Porta aliança']

function serviceLabel(value) {
  return SERVICOS.find(item => item.value === value)?.label || value || 'Serviço não definido'
}

function isLiveService(value) {
  return value === 'PINTURA_AO_VIVO' || value === 'AQUARELA_AO_VIVO'
}

function todayISO() {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function fallbackTipoEvento(tipoServico) {
  if (tipoServico === 'ENCOMENDA_ARTISTICA') return 'Encomenda artística'
  if (tipoServico === 'IDENTIDADE_VISUAL') return 'Identidade visual'
  return ''
}

function nullableText(value) {
  const trimmed = String(value || '').trim()
  return trimmed ? trimmed : null
}

function formatTime(value) {
  return value ? String(value).slice(0, 5) : ''
}

function parseDadosServico(value) {
  if (!value) return {}

  try {
    const parsed = JSON.parse(value)
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
  } catch {
    return {}
  }
}

function buildDadosServico(form) {
  if (form.tipoServico === 'ENCOMENDA_ARTISTICA') {
    return {
      tipoProduto: form.tipoProduto,
      dimensao: form.dimensao,
      tecnica: form.tecnica,
      temaReferencia: form.temaReferencia,
      prazoEstimado: form.prazoEstimado,
      formaEntrega: form.formaEntrega,
      enderecoEnvio: form.enderecoEnvio,
      freteResponsavel: form.formaEntrega === 'ENVIO' ? form.freteResponsavel : '',
    }
  }

  if (form.tipoServico === 'IDENTIDADE_VISUAL') {
    return {
      prazoDiasUteis: form.prazoDiasUteis,
      rodadasAjustes: form.rodadasAjustes,
    }
  }

  return {
    horarioInicio: form.horarioInicio,
    duracaoEstimada: form.duracaoEstimada,
    numeroConvidados: form.numeroConvidados,
    tecnica: form.tecnica,
    tamanhoTela: form.tamanhoTela,
    cenaRetratada: form.cenaRetratada,
    entregaObra: form.entregaObra,
  }
}

function normalizeDadosServico(value) {
  const data = typeof value === 'string' ? parseDadosServico(value) : value || {}
  return JSON.stringify(Object.keys(data).sort().reduce((normalized, key) => {
    normalized[key] = data[key] == null ? '' : String(data[key])
    return normalized
  }, {}))
}

function serviceDetailRows(evento) {
  const dadosServico = parseDadosServico(evento.dadosServico)

  if (evento.tipoServico === 'ENCOMENDA_ARTISTICA') {
    return [
      ['Produto', dadosServico.tipoProduto],
      ['Dimensão', dadosServico.dimensao],
      ['Técnica', dadosServico.tecnica || evento.tecnica],
      ['Prazo estimado', dadosServico.prazoEstimado],
      ['Forma de entrega', dadosServico.formaEntrega === 'RETIRADA' ? 'Retirada' : 'Envio'],
      ['Frete/envio', dadosServico.formaEntrega === 'RETIRADA' ? null : dadosServico.freteResponsavel],
      [dadosServico.formaEntrega === 'RETIRADA' ? 'Local de retirada' : 'Endereço de envio', dadosServico.enderecoEnvio],
      ['Tema/referência', dadosServico.temaReferencia],
    ]
  }

  if (evento.tipoServico === 'IDENTIDADE_VISUAL') {
    return [
      ['Prazo em dias úteis', dadosServico.prazoDiasUteis],
      ['Rodadas de ajustes', dadosServico.rodadasAjustes],
    ]
  }

  return []
}

const STATUS_STEPS = [
  { key: 'SEM_CONTRATO', label: 'Sem contrato', icon: FileText },
  { key: 'RASCUNHO', label: 'Gerado', icon: CheckCircle2 },
  { key: 'ENVIADO', label: 'Pendente de assinatura', icon: Send },
  { key: 'ASSINADO', label: 'Assinado', icon: PenLine },
]

const CHECKLIST = [
  { label: 'Reunião de briefing realizada', done: true, date: '10/10' },
  { label: 'Pagamento do sinal confirmado', done: true, date: '10/10' },
  { label: 'Contrato assinado pelas partes', done: false, badge: 'Pendente' },
  { label: 'Envio de planta técnica para buffet', done: false, badge: 'Prazo: 24/10' },
]

function taskStatusLabel(status) {
  const map = {
    A_FAZER: 'A fazer',
    EM_ANDAMENTO: 'Em andamento',
    CONCLUIDA: 'Concluída',
  }
  return map[status] || status
}

function taskStatusClass(status) {
  const map = {
    A_FAZER: 'status-rascunho',
    EM_ANDAMENTO: 'status-pendente',
    CONCLUIDA: 'status-assinado',
  }
  return map[status] || 'status-rascunho'
}

function taskChecklistProgress(task) {
  const items = task.checklistItems || []
  if (!items.length) return ''
  return `${items.filter(item => item.concluido).length}/${items.length}`
}

function getCurrentStepIndex(status) {
  const idx = STATUS_STEPS.findIndex(s => s.key === status)
  return idx === -1 ? 0 : idx
}

function getContractStatusLabel(status) {
  const map = {
    SEM_CONTRATO: 'Sem contrato',
    DISPENSADO: 'Sem contrato necessário',
    RASCUNHO: 'Gerado',
    ENVIADO: 'Pendente de assinatura',
    ASSINADO: 'Assinado',
  }
  return map[status] || status
}

function getContractStatusClass(status) {
  const map = {
    SEM_CONTRATO: 'status-sem-contrato',
    DISPENSADO: 'status-sem-contrato',
    RASCUNHO: 'status-rascunho',
    ENVIADO: 'status-pendente',
    ASSINADO: 'status-assinado',
  }
  return map[status] || 'status-rascunho'
}

export default function EventoDetalhe() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [deleteTaskCandidate, setDeleteTaskCandidate] = React.useState(null)
  const taskManager = useTasksPage(id || '')
  const [paymentForm, setPaymentForm] = React.useState(null)
  const [costDefaults, setCostDefaults] = React.useState({})

  React.useEffect(() => {
    financialService.getCostDefaults()
      .then(data => {
        const map = {}
        if (Array.isArray(data)) data.forEach(d => { map[d.tipoServico] = Number(d.custoDefault) || 0 })
        setCostDefaults(map)
      })
      .catch(() => {})
  }, [])

  const {
    additionalSigners,
    addAdditionalSigner,
    canReviewContract,
    canSendContract,
    clientes,
    confirmDialog,
    contrato,
    contractualFieldsLocked,
    currentContractStatus,
    editOpen,
    eventForm,
    evento,
    handleReviewContract,
    handleSaveEvent,
    handleSend,
    handleViewPdf,
    loading,
    openEditModal,
    openSendModal,
    pdfOpen,
    removeAdditionalSigner,
    reviewing,
    savePayment,
    savingEvent,
    savingPayment,
    sendActionLabel,
    sending,
    sendOpen,
    setConfirmDialog,
    setEditOpen,
    setEventField,
    setPdfOpen,
    setSendOpen,
    toast,
    updateAdditionalSigner,
  } = useEventDetailPage({
    id,
    shouldOpenEdit: searchParams.get('edit') === '1',
    onEditParamHandled: event => navigate(`/eventos/${event.id}`, { replace: true }),
    helpers: {
      buildDadosServico,
      fallbackTipoEvento,
      formatCurrencyInput,
      formatTime,
      isLiveService,
      normalizeDadosServico,
      nullableText,
      parseCurrency,
      parseDadosServico,
      produtosEncomenda: PRODUTOS_ENCOMENDA,
      todayISO,
    },
  })

  const linkedEventTasks = taskManager.tasks

  const openCreateLinkedTask = () => {
    taskManager.openCreate({
      eventId: String(evento.id),
      tipoServico: evento.tipoServico || '',
    })
  }

  const confirmDeleteLinkedTask = async () => {
    const task = deleteTaskCandidate
    setDeleteTaskCandidate(null)
    await taskManager.deleteTask(task)
  }
  const stepIdx = getCurrentStepIndex(currentContractStatus)

  React.useEffect(() => {
    if (!evento) return
    setPaymentForm({
      sinalRecebido: evento.sinalRecebido || false,
      dataRecebimentoSinal: evento.dataRecebimentoSinal || '',
      restanteRecebido: evento.restanteRecebido || false,
      dataRecebimentoRestante: evento.dataRecebimentoRestante || '',
      observacaoPagamento: evento.observacaoPagamento || '',
      custoTotal: evento.custoTotal != null ? String(evento.custoTotal) : '',
      custosExtras: evento.custosExtras != null ? String(evento.custosExtras) : '',
    })
  }, [evento])

  if (loading) {
    return (
      <>
        <TopBar breadcrumb={[{ label: 'Eventos', href: '/eventos' }, { label: 'Dashboard', href: '/' }]} title="Carregando..." />
        <div className="page-content event-detail-loading">
          <span className="spinner spinner-lg"></span>
        </div>
      </>
    )
  }

  if (!evento) {
    return (
      <>
        <TopBar breadcrumb={[{ label: 'Eventos', href: '/eventos' }]} title="Não encontrado" />
        <div className="page-content">
          <div className="empty-state">
            <h3>Evento não encontrado</h3>
            <p>Este evento pode ter sido deletado ou não existe.</p>
            <button className="btn btn-secondary" onClick={() => navigate('/eventos')}>Voltar para Eventos</button>
          </div>
        </div>
      </>
    )
  }

  const serviceRows = serviceDetailRows(evento).filter(([, value]) => value)

  return (
    <>
      <TopBar breadcrumb={[{ label: 'Eventos', href: '/eventos' }, { label: 'Dashboard', href: '/' }]} title={evento.client?.nome || 'Evento'} />
      <div className="page-content">
        <div className="page-header event-detail-header">
          <div className="page-header-left">
            <div className="event-detail-title-row">
              <h1 className="event-detail-title">{evento.client?.nome || 'Cliente Desconhecido'}</h1>
              <span className={`status-badge ${getContractStatusClass(currentContractStatus)}`}>
                {getContractStatusLabel(currentContractStatus)}
              </span>
            </div>
            <p className="event-detail-subtitle">
              <span>{evento.tipoEvento || '-'}</span>
              <span>.</span>
              <span>{serviceLabel(evento.tipoServico)}</span>
              <span>.</span>
              <span>{formatDateBR(evento.data, { day: '2-digit', month: 'long', year: 'numeric' }) || 'Data não definida'}</span>
            </p>
          </div>
          <div className="event-detail-actions">
            {canReviewContract && (
              <button className="btn btn-secondary" onClick={handleReviewContract} disabled={reviewing}>
                <AlertTriangle size={14} /> {reviewing ? 'Revisando...' : 'Revisar contrato'}
              </button>
            )}
            <button className="btn btn-primary" onClick={openEditModal}><Pencil size={14} /> Editar Evento</button>
          </div>
        </div>

        <div className="card contract-status-card">
          <div className="flex-between contract-status-header">
            <span className="contract-status-title">Status do Contrato</span>
          </div>
          {currentContractStatus === 'DISPENSADO' ? (
            <p className="text-secondary" style={{ margin: 0 }}>
              Este evento foi marcado como <strong>sem contrato necessário</strong>. Para gerar um contrato, edite o evento e desmarque essa opção.
            </p>
          ) : (
            <div className="contract-stepper">
              {STATUS_STEPS.map((step, i) => {
                const Icon = step.icon
                const done = i < stepIdx
                const active = i === stepIdx
                return (
                  <React.Fragment key={step.key}>
                    <div className="contract-step">
                      <div className={`contract-step-circle ${done || active ? 'active' : ''}`}>
                        <Icon size={14} />
                      </div>
                      <span className={`contract-step-label ${active || done ? 'active' : ''}`}>
                        {step.label}
                      </span>
                    </div>
                    {i < STATUS_STEPS.length - 1 && (
                      <div className={`contract-step-connector ${i < stepIdx ? 'done' : ''}`} />
                    )}
                  </React.Fragment>
                )
              })}
            </div>
          )}
        </div>

        <div className="event-detail-grid">
          <div className="card">
            <div className="event-card-header">
              <div className="event-card-title">
                <MapPin size={15} color="var(--primary)" /> Info do Evento
              </div>
            </div>

            <div className="event-info-grid">
              <div>
                <div className="event-info-label">Serviço</div>
                <div className="event-info-value">{serviceLabel(evento.tipoServico)}</div>
              </div>
              {isLiveService(evento.tipoServico) && (
                <>
                  <div>
                    <div className="event-info-label">Local</div>
                    <div className="event-info-value">{evento.local || 'Não definido'}</div>
                  </div>
                  <div>
                    <div className="event-info-label">Convidados</div>
                    <div className="event-info-value">{evento.numeroConvidados || '-'} pessoas</div>
                  </div>
                  <div>
                    <div className="event-info-label">Horário</div>
                    <div className="event-info-value">{formatTime(evento.horarioInicio) || '-'}</div>
                  </div>
                  <div>
                    <div className="event-info-label">Duração</div>
                    <div className="event-info-value">{evento.duracaoEstimada || '-'}</div>
                  </div>
                </>
              )}
            </div>

            {isLiveService(evento.tipoServico) && (
              <div className="event-info-grid with-border">
                <div>
                  <div className="event-info-label">Técnica</div>
                  <div className="event-info-value">{evento.tecnica || '-'}</div>
                </div>
                <div>
                  <div className="event-info-label">Tamanho da tela</div>
                  <div className="event-info-value">{evento.tamanhoTela || '-'}</div>
                </div>
                <div className="event-info-full">
                  <div className="event-info-label">Cena retratada</div>
                  <div className="event-info-text">{evento.cenaRetratada || '-'}</div>
                </div>
                <div className="event-info-full">
                  <div className="event-info-label">Entrega da obra</div>
                  <div className="event-info-text">{evento.entregaObra || '-'}</div>
                </div>
              </div>
            )}

            {serviceRows.length > 0 && (
              <div className="event-info-grid with-border">
                {serviceRows.map(([label, value]) => (
                  <div key={label} className={String(value).length > 80 ? 'event-info-full' : ''}>
                    <div className="event-info-label">{label}</div>
                    <div className="event-info-text">{value}</div>
                  </div>
                ))}
              </div>
            )}

            <div className="event-info-block">
              <div className="event-info-label spaced">Observações</div>
              <div className="event-info-text">
                {evento.observacoes || 'Nenhuma observacao cadastrada.'}
              </div>
            </div>

            <div className="event-info-grid with-border last">
              <div>
                <div className="event-info-label">Orcamento Total</div>
                <div className="event-money-value">R$ {evento.valor ? Number(evento.valor).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) : '0,00'}</div>
              </div>
              <div>
                <div className="event-info-label">Sinal Pago</div>
                <div className="event-money-value event-money-muted">R$ {evento.valorSinal ? Number(evento.valorSinal).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) : '0,00'}</div>
              </div>
              <div>
                <div className="event-info-label">Pagamento sinal</div>
                <div className="event-info-text">{evento.formaPagamentoSinal || '-'}</div>
              </div>
              <div>
                <div className="event-info-label">Pagamento restante</div>
                <div className="event-info-text">{evento.formaPagamentoRestante || '-'}</div>
              </div>
            </div>

            {paymentForm && (
              <div className="event-info-block" style={{ borderTop: '1px solid var(--border)', paddingTop: '16px', marginTop: '4px' }}>
                <div className="event-card-header mb-14" style={{ marginBottom: '14px' }}>
                  <div className="event-card-title" style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600, fontSize: '14px' }}>
                    Controle de Pagamento
                  </div>
                </div>
                <div className="form-grid-2" style={{ gap: '12px', marginBottom: '12px' }}>
                  <label className="form-group" style={{ marginBottom: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                      <input
                        type="checkbox"
                        checked={paymentForm.sinalRecebido}
                        onChange={e => setPaymentForm(f => ({ ...f, sinalRecebido: e.target.checked, dataRecebimentoSinal: e.target.checked ? (f.dataRecebimentoSinal || new Date().toISOString().slice(0, 10)) : '' }))}
                      />
                      <span className="form-label" style={{ marginBottom: 0 }}>Sinal recebido</span>
                    </div>
                    {paymentForm.sinalRecebido && (
                      <input
                        type="date"
                        className="form-input"
                        value={paymentForm.dataRecebimentoSinal}
                        onChange={e => setPaymentForm(f => ({ ...f, dataRecebimentoSinal: e.target.value }))}
                      />
                    )}
                  </label>
                  <label className="form-group" style={{ marginBottom: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                      <input
                        type="checkbox"
                        checked={paymentForm.restanteRecebido}
                        onChange={e => setPaymentForm(f => ({ ...f, restanteRecebido: e.target.checked, dataRecebimentoRestante: e.target.checked ? (f.dataRecebimentoRestante || new Date().toISOString().slice(0, 10)) : '' }))}
                      />
                      <span className="form-label" style={{ marginBottom: 0 }}>Pagamento final recebido</span>
                    </div>
                    {paymentForm.restanteRecebido && (
                      <input
                        type="date"
                        className="form-input"
                        value={paymentForm.dataRecebimentoRestante}
                        onChange={e => setPaymentForm(f => ({ ...f, dataRecebimentoRestante: e.target.value }))}
                      />
                    )}
                  </label>
                </div>
                <label className="form-group" style={{ marginBottom: '12px' }}>
                  <span className="form-label">Observação de pagamento</span>
                  <textarea
                    className="form-input"
                    rows={2}
                    placeholder="Anotações sobre o pagamento..."
                    value={paymentForm.observacaoPagamento}
                    onChange={e => setPaymentForm(f => ({ ...f, observacaoPagamento: e.target.value }))}
                  />
                </label>
                <div className="form-grid-2" style={{ gap: '12px', marginBottom: '12px' }}>
                  <label className="form-group" style={{ marginBottom: 0 }}>
                    <span className="form-label">Custo padrão do evento (R$)</span>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      className="form-input"
                      placeholder={`${(costDefaults[evento.tipoServico] ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })} (padrão do tipo)`}
                      value={paymentForm.custoTotal}
                      onChange={e => setPaymentForm(f => ({ ...f, custoTotal: e.target.value }))}
                    />
                  </label>
                  <label className="form-group" style={{ marginBottom: 0 }}>
                    <span className="form-label">Custos extras (R$)</span>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      className="form-input"
                      placeholder="0,00"
                      value={paymentForm.custosExtras}
                      onChange={e => setPaymentForm(f => ({ ...f, custosExtras: e.target.value }))}
                    />
                  </label>
                </div>
                <button
                  className="btn btn-secondary"
                  onClick={() => savePayment({
                    sinalRecebido: paymentForm.sinalRecebido,
                    dataRecebimentoSinal: paymentForm.dataRecebimentoSinal || null,
                    restanteRecebido: paymentForm.restanteRecebido,
                    dataRecebimentoRestante: paymentForm.dataRecebimentoRestante || null,
                    observacaoPagamento: paymentForm.observacaoPagamento || null,
                    custoTotal: paymentForm.custoTotal !== '' ? Number(paymentForm.custoTotal) : null,
                    custosExtras: paymentForm.custosExtras !== '' ? Number(paymentForm.custosExtras) : null,
                  })}
                  disabled={savingPayment}
                >
                  {savingPayment ? <span className="spinner spinner-sm" /> : <Save size={14} />}
                  Salvar pagamento
                </button>
              </div>
            )}
          </div>

          <div className="event-side-column">
            <div className="card">
              <div className="event-card-header mb-14">
                <div className="event-card-title">
                  <FileText size={15} color="var(--primary)" /> Contrato
                </div>
                <span className={`status-badge ${getContractStatusClass(currentContractStatus)}`}>
                  {getContractStatusLabel(currentContractStatus)}
                </span>
              </div>

              {contrato ? (
                <>
                  <div className="contract-file-card">
                    <div className="contract-file-icon">PDF</div>
                    <div className="contract-file-body">
                      <div className="contract-file-title">{contrato.template?.nome || 'Contrato Base'}</div>
                      <div className="contract-file-subtitle">{contrato.pdfUrl ? 'PDF gerado com sucesso' : 'Erro ao gerar PDF'}</div>
                    </div>
                  </div>
                  <div className="contract-link-row">
                    <button
                      className="btn btn-ghost contract-open-link"
                      onClick={handleViewPdf}
                    >
                      Ver PDF
                    </button>
                  </div>
                  {canSendContract && (
                    <button className="btn btn-primary contract-send-full" onClick={openSendModal} disabled={sending}>
                      <ExternalLink size={15} /> {sending ? 'Enviando...' : sendActionLabel}
                    </button>
                  )}
                </>
              ) : evento.contratoDispensado ? (
                <div className="empty-state">
                  <FileText size={40} className="icon-muted" />
                  <h3>Sem contrato necessário</h3>
                  <p>Este evento foi marcado como dispensado de contrato.</p>
                </div>
              ) : (
                <div className="empty-state">
                  <FileText size={40} className="icon-muted" />
                  <h3>Nenhum contrato ativo</h3>
                  <p>Ainda não há um contrato gerado para este evento.</p>
                  <button className="btn btn-secondary" onClick={() => navigate(`/contratos/gerar?eventId=${evento.id}`)}>
                    + Gerar contrato
                  </button>
                </div>
              )}
            </div>

            <div className="card">
              <div className="event-card-header mb-14 client-section-title">
                <div className="checklist-title">Checklist Operacional</div>
                <button className="action-btn" title="Criar tarefa para este evento" onClick={openCreateLinkedTask}>
                  <CalendarPlus size={14} />
                </button>
              </div>
              <div className="checklist-list">
                {linkedEventTasks.map(task => {
                  const done = task.status === 'CONCLUIDA'
                  const progress = taskChecklistProgress(task)
                  return (
                  <div key={task.id} className={`checklist-item ${done ? 'done' : ''}`}>
                    {done
                      ? <CheckSquare size={16} color="var(--success)" />
                      : (
                        <button className="event-task-check-btn" type="button" onClick={() => taskManager.changeStatus(task, 'CONCLUIDA')} title="Concluir tarefa">
                          <Square size={16} color="var(--text-muted)" />
                        </button>
                      )}
                    <button type="button" className={`checklist-label event-task-link ${done ? 'done' : ''}`} onClick={() => taskManager.openTask(task)}>
                      {task.titulo}
                    </button>
                    <span className="checklist-date">{formatDateBR(task.prazoData)}{task.prazoHorario ? ` ${String(task.prazoHorario).slice(0, 5)}` : ''}</span>
                    <span className={`status-badge checklist-badge ${taskStatusClass(task.status)}`}>{progress ? `${progress} - ${taskStatusLabel(task.status)}` : taskStatusLabel(task.status)}</span>
                  </div>
                  )
                })}
                {linkedEventTasks.length === 0 && CHECKLIST.length >= 0 && (
                  <div className="empty-state empty-state-compact">
                    <p>Nenhuma tarefa vinculada a este evento.</p>
                    <button className="btn btn-secondary" onClick={openCreateLinkedTask}>
                      Criar tarefa
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {pdfOpen && (
        <div className="modal-overlay">
          <div className="pdf-modal">
            <div className="pdf-modal-header">
              <div>
                <strong>{contrato?.template?.nome || 'Contrato'}</strong>
                <span>{evento.client?.nome || 'Cliente'}</span>
              </div>
              <button className="topbar-icon-btn" onClick={() => setPdfOpen(false)} title="Fechar preview">
                <X size={16} />
              </button>
            </div>
            <iframe
              title="PDF do contrato"
              className="pdf-modal-frame"
              src={`${contractsService.pdfUrl(contrato.id)}#toolbar=1&navpanes=0&view=FitH`}
            />
          </div>
        </div>
      )}

      {sendOpen && (
        <div className="modal-overlay">
          <div className="event-edit-modal signature-modal">
            <div className="pdf-modal-header">
              <div>
                <strong>{sendActionLabel}</strong>
                <span>Adicione signatários extras se outras pessoas também precisarem assinar.</span>
              </div>
              <button type="button" className="topbar-icon-btn" onClick={() => setSendOpen(false)} title="Fechar envio">
                <X size={16} />
              </button>
            </div>

            <div className="event-edit-body">
              <div className="event-edit-warning">
                O cliente principal já será incluído automaticamente como signatário. A conta da API continua assinando automaticamente quando estiver habilitada.
              </div>

              {additionalSigners.length === 0 ? (
                <div className="empty-state signature-empty-state">
                  <p>Nenhum signatário extra adicionado.</p>
                </div>
              ) : (
                <div className="signature-signers-list">
                  {additionalSigners.map((signer, index) => (
                    <div key={index} className="form-grid-2 signature-signer-row">
                      <label className="form-group">
                        <span className="form-label">Nome</span>
                        <input className="form-input" value={signer.nome} onChange={e => updateAdditionalSigner(index, 'nome', e.target.value)} placeholder="Nome do signatário" />
                      </label>
                      <label className="form-group">
                        <span className="form-label">E-mail</span>
                        <input className="form-input" type="email" value={signer.email} onChange={e => updateAdditionalSigner(index, 'email', e.target.value)} placeholder="email@exemplo.com" />
                      </label>
                      <button type="button" className="btn btn-secondary signature-remove-btn" onClick={() => removeAdditionalSigner(index)}>
                        Remover signatário
                      </button>
                    </div>
                  ))}
                </div>
              )}

              <button type="button" className="btn btn-secondary signature-add-btn" onClick={addAdditionalSigner}>
                + Adicionar signatário
              </button>
            </div>

            <div className="form-footer signature-modal-footer">
              <button type="button" className="btn btn-secondary" onClick={() => setSendOpen(false)}>Cancelar</button>
              <button type="button" className="btn btn-primary" onClick={handleSend} disabled={sending}>
                <Send size={15} /> {sending ? 'Enviando...' : 'Enviar para assinatura'}
              </button>
            </div>
          </div>
        </div>
      )}

      {editOpen && eventForm && (
        <div className="modal-overlay">
          <form className="event-edit-modal" onSubmit={handleSaveEvent}>
            <div className="pdf-modal-header">
              <div>
                <strong>Editar evento</strong>
                <span>{getContractStatusLabel(currentContractStatus)}</span>
              </div>
              <button type="button" className="topbar-icon-btn" onClick={() => setEditOpen(false)} title="Fechar edição">
                <X size={16} />
              </button>
            </div>

            {currentContractStatus === 'RASCUNHO' && (
              <div className="event-edit-warning">
                Alterar dados contratuais invalida o contrato gerado e exige gerar um novo PDF.
              </div>
            )}

            {contractualFieldsLocked && (
              <div className="event-edit-warning">
                Dados contratuais bloqueados neste status. Você pode editar apenas observações internas.
              </div>
            )}

            <div className="event-edit-body">
              <div className="form-grid-2">
                <label className="form-group">
                  <span className="form-label">Cliente</span>
                  <select className="form-select" value={eventForm.clienteId} onChange={e => setEventField('clienteId', e.target.value)} disabled={contractualFieldsLocked} required>
                    <option value="" disabled>Selecione um cliente</option>
                    {clientes.map(client => (
                      <option key={client.id} value={client.id}>{client.nome}</option>
                    ))}
                    {evento.client?.id && !clientes.some(client => String(client.id) === String(evento.client.id)) && (
                      <option value={evento.client.id}>{evento.client.nome}</option>
                    )}
                  </select>
                </label>

                <label className="form-group">
                  <span className="form-label">Serviço contratado</span>
                  <select className="form-select" value={eventForm.tipoServico} onChange={e => setEventField('tipoServico', e.target.value)} disabled={contractualFieldsLocked}>
                    {SERVICOS.map(opcaoServico => <option key={opcaoServico.value} value={opcaoServico.value}>{opcaoServico.label}</option>)}
                  </select>
                </label>

                {isLiveService(eventForm.tipoServico) && (
                  <>
                    <label className="form-group">
                      <span className="form-label">Tipo de evento</span>
                      <select className="form-select" value={eventForm.tipoEvento} onChange={e => setEventField('tipoEvento', e.target.value)} disabled={contractualFieldsLocked} required>
                        {TIPOS.map(tipo => <option key={tipo} value={tipo}>{tipo}</option>)}
                        {!TIPOS.includes(eventForm.tipoEvento) && <option value={eventForm.tipoEvento}>{eventForm.tipoEvento}</option>}
                      </select>
                    </label>

                    <label className="form-group">
                      <span className="form-label">Data</span>
                      <input className="form-input" type="date" value={eventForm.data} onChange={e => setEventField('data', e.target.value)} disabled={contractualFieldsLocked} required />
                    </label>

                    <label className="form-group">
                      <span className="form-label">Horário de início</span>
                      <input className="form-input" type="time" value={eventForm.horarioInicio} onChange={e => setEventField('horarioInicio', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                    <label className="form-group">
                      <span className="form-label">Duração estimada</span>
                      <input className="form-input" value={eventForm.duracaoEstimada} onChange={e => setEventField('duracaoEstimada', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                    <label className="form-group">
                      <span className="form-label">Número de convidados</span>
                      <input className="form-input" type="number" min="0" value={eventForm.numeroConvidados} onChange={e => setEventField('numeroConvidados', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>
                  </>
                )}

                <label className="form-group">
                  <span className="form-label">Valor total</span>
                  <CurrencyInput value={eventForm.valor} onChange={value => setEventField('valor', value)} disabled={contractualFieldsLocked} required />
                </label>

                <label className="form-group">
                  <span className="form-label">Valor sinal</span>
                  <CurrencyInput value={eventForm.valorSinal} onChange={value => setEventField('valorSinal', value)} disabled={contractualFieldsLocked} />
                </label>

                <label className="form-group">
                  <span className="form-label">Forma pagamento sinal</span>
                  <input className="form-input" value={eventForm.formaPagamentoSinal} onChange={e => setEventField('formaPagamentoSinal', e.target.value)} disabled={contractualFieldsLocked} />
                </label>

                <label className="form-group">
                  <span className="form-label">Forma pagamento restante</span>
                  <input className="form-input" value={eventForm.formaPagamentoRestante} onChange={e => setEventField('formaPagamentoRestante', e.target.value)} disabled={contractualFieldsLocked} />
                </label>

                {isLiveService(eventForm.tipoServico) && (
                  <>
                    <label className="form-group full">
                      <span className="form-label">Local</span>
                      <input className="form-input" value={eventForm.local} onChange={e => setEventField('local', e.target.value)} disabled={contractualFieldsLocked} required />
                    </label>

                    <label className="form-group">
                      <span className="form-label">Técnica</span>
                      <input className="form-input" value={eventForm.tecnica} onChange={e => setEventField('tecnica', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                    <label className="form-group">
                      <span className="form-label">Tamanho da tela</span>
                      <input className="form-input" value={eventForm.tamanhoTela} onChange={e => setEventField('tamanhoTela', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                    <label className="form-group full">
                      <span className="form-label">Cena retratada</span>
                      <textarea className="form-textarea" rows={3} value={eventForm.cenaRetratada} onChange={e => setEventField('cenaRetratada', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                    <label className="form-group full">
                      <span className="form-label">Entrega da obra</span>
                      <input className="form-input" value={eventForm.entregaObra} onChange={e => setEventField('entregaObra', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>
                  </>
                )}

                {eventForm.tipoServico === 'ENCOMENDA_ARTISTICA' && (
                  <>
                    <div className="form-section-title edit-section-title">Encomenda artística</div>

                    <label className="form-group">
                      <span className="form-label">Tipo de produto</span>
                      <select className="form-select" value={eventForm.tipoProduto} onChange={e => setEventField('tipoProduto', e.target.value)} disabled={contractualFieldsLocked}>
                        {PRODUTOS_ENCOMENDA.map(produto => <option key={produto} value={produto}>{produto}</option>)}
                      </select>
                    </label>

                    <label className="form-group">
                      <span className="form-label">Dimensão</span>
                      <input className="form-input" value={eventForm.dimensao} onChange={e => setEventField('dimensao', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                    <label className="form-group">
                      <span className="form-label">Técnica</span>
                      <input className="form-input" value={eventForm.tecnica} onChange={e => setEventField('tecnica', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                    <label className="form-group">
                      <span className="form-label">Prazo estimado</span>
                      <input className="form-input" value={eventForm.prazoEstimado} onChange={e => setEventField('prazoEstimado', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                    <label className="form-group">
                      <span className="form-label">Forma de entrega</span>
                      <select className="form-select" value={eventForm.formaEntrega} onChange={e => setEventField('formaEntrega', e.target.value)} disabled={contractualFieldsLocked}>
                        <option value="ENVIO">Envio</option>
                        <option value="RETIRADA">Retirada</option>
                      </select>
                    </label>

                    {eventForm.formaEntrega === 'ENVIO' && (
                      <label className="form-group">
                        <span className="form-label">Frete/responsável pelo envio</span>
                        <input className="form-input" value={eventForm.freteResponsavel} onChange={e => setEventField('freteResponsavel', e.target.value)} disabled={contractualFieldsLocked} />
                      </label>
                    )}
                    <label className="form-group full">
                      <span className="form-label">{eventForm.formaEntrega === 'RETIRADA' ? 'Local de retirada' : 'Endereço de envio'}</span>
                      <input className="form-input" value={eventForm.enderecoEnvio} onChange={e => setEventField('enderecoEnvio', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                    <label className="form-group full">
                      <span className="form-label">Tema/referência</span>
                      <textarea className="form-textarea" rows={3} value={eventForm.temaReferencia} onChange={e => setEventField('temaReferencia', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>
                  </>
                )}

                {eventForm.tipoServico === 'IDENTIDADE_VISUAL' && (
                  <>
                    <div className="form-section-title edit-section-title">Identidade visual</div>

                    <label className="form-group">
                      <span className="form-label">Prazo em dias úteis</span>
                      <input className="form-input" type="number" min="0" value={eventForm.prazoDiasUteis} onChange={e => setEventField('prazoDiasUteis', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                    <label className="form-group">
                      <span className="form-label">Rodadas de ajustes</span>
                      <input className="form-input" type="number" min="0" value={eventForm.rodadasAjustes} onChange={e => setEventField('rodadasAjustes', e.target.value)} disabled={contractualFieldsLocked} />
                    </label>

                  </>
                )}

                <label className="form-group full">
                  <span className="form-label">Observações internas</span>
                  <textarea className="form-textarea" rows={4} value={eventForm.observacoes} onChange={e => setEventField('observacoes', e.target.value)} />
                </label>

                <label className="form-group full" style={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: '8px', marginBottom: 0 }}>
                  <input
                    type="checkbox"
                    checked={eventForm.contratoDispensado}
                    disabled={Boolean(contrato)}
                    onChange={e => setEventField('contratoDispensado', e.target.checked)}
                  />
                  <span className="form-label" style={{ marginBottom: 0 }}>
                    Este evento não terá contrato
                    {contrato ? ' (não pode ser alterado com um contrato já gerado)' : ''}
                  </span>
                </label>
              </div>
            </div>

            <div className="form-footer signature-modal-footer">
              <button type="button" className="btn btn-secondary" onClick={() => setEditOpen(false)}>Cancelar</button>
              <button type="submit" className="btn btn-primary" disabled={savingEvent}>
                <Save size={15} /> {savingEvent ? 'Salvando...' : 'Salvar alterações'}
              </button>
            </div>
          </form>
        </div>
      )}

      <TaskModal
        addChecklistItem={taskManager.addChecklistItem}
        addComment={taskManager.addComment}
        commentText={taskManager.commentText}
        deleteAttachment={taskManager.deleteAttachment}
        deleteComment={taskManager.deleteComment}
        events={taskManager.events}
        form={taskManager.form}
        modalOpen={taskManager.modalOpen}
        onDeleteTask={setDeleteTaskCandidate}
        saveTask={taskManager.saveTask}
        saving={taskManager.saving}
        selectedTask={taskManager.selectedTask}
        setCommentText={taskManager.setCommentText}
        setField={taskManager.setField}
        setModalOpen={taskManager.setModalOpen}
        updateChecklistItem={taskManager.updateChecklistItem}
        removeChecklistItem={taskManager.removeChecklistItem}
        uploadAttachment={taskManager.uploadAttachment}
      />

      <ConfirmDialog
        open={Boolean(confirmDialog)}
        title={confirmDialog?.title}
        description={confirmDialog?.description}
        confirmLabel={confirmDialog?.confirmLabel}
        tone={confirmDialog?.tone}
        loading={savingEvent || reviewing}
        onCancel={() => setConfirmDialog(null)}
        onConfirm={confirmDialog?.onConfirm}
      />

      <ConfirmDialog
        open={Boolean(deleteTaskCandidate)}
        title="Excluir tarefa?"
        description="Essa ação remove a tarefa, seus comentários, checklist, etiquetas e anexos. Essa operação não pode ser desfeita."
        confirmLabel="Excluir tarefa"
        tone="danger"
        onCancel={() => setDeleteTaskCandidate(null)}
        onConfirm={confirmDeleteLinkedTask}
      />

      {toast && <div className={`toast ${toast.type}`}>{toast.msg}</div>}
      {taskManager.toast && <div className={`toast ${taskManager.toast.type}`}>{taskManager.toast.msg}</div>}
    </>
  )
}

