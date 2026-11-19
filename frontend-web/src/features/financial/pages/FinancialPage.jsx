import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import TopBar from '../../../layouts/TopBar'
import { TrendingUp, TrendingDown, Clock, CircleDollarSign, AlertCircle, Settings, ChevronDown, ChevronUp, Minus } from 'lucide-react'
import { formatDateBR } from '../../../utils/date'
import { useFinancialPage } from '../hooks/useFinancialPage'

const MESES = [
  { value: 1, label: 'Janeiro' }, { value: 2, label: 'Fevereiro' }, { value: 3, label: 'Março' },
  { value: 4, label: 'Abril' }, { value: 5, label: 'Maio' }, { value: 6, label: 'Junho' },
  { value: 7, label: 'Julho' }, { value: 8, label: 'Agosto' }, { value: 9, label: 'Setembro' },
  { value: 10, label: 'Outubro' }, { value: 11, label: 'Novembro' }, { value: 12, label: 'Dezembro' },
]

const TIPO_LABELS = {
  PINTURA_AO_VIVO: 'Pintura ao Vivo',
  AQUARELA_AO_VIVO: 'Aquarela ao Vivo',
  ENCOMENDA_ARTISTICA: 'Encomenda Artística',
  IDENTIDADE_VISUAL: 'Identidade Visual',
}

function formatBRL(value) {
  return Number(value || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export default function FinancialPage() {
  const navigate = useNavigate()
  const [costConfigOpen, setCostConfigOpen] = useState(false)
  const [draftCosts, setDraftCosts] = useState({})

  const {
    anosDisponiveis,
    breakdownServico,
    costDefaults,
    custoTotalPeriodo,
    eventosFiltrados,
    loading,
    lucroTotal,
    markingId,
    mostrarTodos,
    pagamentosPendentes,
    pagamentosRecebidos,
    periodoAno,
    periodoMes,
    receitaRecebida,
    receitaTotal,
    savingCostDefault,
    setMostrarTodos,
    setPeriodoAno,
    setPeriodoMes,
    sinaisPendentes,
    toast,
    valorRestantesPendentes,
    valorSinaisPendentes,
    handleMarkSinal,
    handleMarkRestante,
    handleUpdateCostDefault,
    effectiveCost,
  } = useFinancialPage()

  const handleOpenCostConfig = () => {
    if (!costConfigOpen) {
      const draft = {}
      Object.entries(costDefaults).forEach(([tipo, val]) => { draft[tipo] = String(val) })
      setDraftCosts(draft)
    }
    setCostConfigOpen(o => !o)
  }

  const margem = receitaRecebida > 0 ? ((lucroTotal / receitaRecebida) * 100) : 0

  return (
    <>
      <TopBar
        breadcrumb={[{ label: 'Dashboard', href: '/' }]}
        title="Financeiro"
      />
      <div className="page-content">
        <div className="page-header">
          <div className="page-header-left">
            <h1>Financeiro</h1>
            <p>Acompanhe receitas, custos, pagamentos pendentes e recebidos.</p>
          </div>
        </div>

        <div className="table-toolbar" style={{ marginBottom: '8px' }}>
          <div className="toolbar-inline" style={{ justifyContent: 'space-between', width: '100%' }}>
            <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
              <select
                className="form-select"
                value={mostrarTodos ? 'todos' : 'periodo'}
                onChange={e => setMostrarTodos(e.target.value === 'todos')}
              >
                <option value="periodo">Período específico</option>
                <option value="todos">Todos os períodos</option>
              </select>
              {!mostrarTodos && (
                <>
                  <select
                    className="form-select"
                    value={periodoMes}
                    onChange={e => setPeriodoMes(Number(e.target.value))}
                  >
                    {MESES.map(m => (
                      <option key={m.value} value={m.value}>{m.label}</option>
                    ))}
                  </select>
                  <select
                    className="form-select"
                    value={periodoAno}
                    onChange={e => setPeriodoAno(Number(e.target.value))}
                  >
                    {(anosDisponiveis.length > 0 ? anosDisponiveis : [new Date().getFullYear()]).map(ano => (
                      <option key={ano} value={ano}>{ano}</option>
                    ))}
                  </select>
                </>
              )}
            </div>
            <button
              className="btn btn-secondary"
              style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
              onClick={handleOpenCostConfig}
            >
              <Settings size={14} />
              Custos padrão
              {costConfigOpen ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
            </button>
          </div>
        </div>

        {costConfigOpen && (
          <div className="card" style={{ marginBottom: '16px' }}>
            <div className="card-section-header">
              <h2 className="card-section-title">Custos Padrão por Tipo de Serviço</h2>
              <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: 0 }}>
                Valores aplicados automaticamente quando o evento não tem custo individualizado.
              </p>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '12px', padding: '0 16px 16px' }}>
              {Object.keys(TIPO_LABELS).map(tipo => (
                <div key={tipo} style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <span className="form-label" style={{ marginBottom: 0 }}>{TIPO_LABELS[tipo]}</span>
                  <div style={{ display: 'flex', gap: '6px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: '6px', padding: '0 10px', flex: 1 }}>
                      <span style={{ color: 'var(--text-muted)', fontSize: '13px', marginRight: '4px' }}>R$</span>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        className="form-input"
                        style={{ border: 'none', padding: '6px 0', background: 'transparent', flex: 1 }}
                        value={draftCosts[tipo] ?? String(costDefaults[tipo] ?? 0)}
                        onChange={e => setDraftCosts(prev => ({ ...prev, [tipo]: e.target.value }))}
                      />
                    </div>
                    <button
                      className="btn btn-secondary"
                      style={{ whiteSpace: 'nowrap', padding: '6px 12px', fontSize: '13px' }}
                      disabled={savingCostDefault === tipo}
                      onClick={() => handleUpdateCostDefault(tipo, draftCosts[tipo] ?? costDefaults[tipo] ?? 0)}
                    >
                      {savingCostDefault === tipo ? <span className="spinner spinner-sm" /> : 'Salvar'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {loading ? (
          <div className="empty-state"><span className="spinner" /></div>
        ) : (
          <>
            <div className="stats-grid" style={{ marginBottom: '24px' }}>
              <div className="stat-card">
                <div className="stat-card-header">
                  <span className="stat-card-label">Receita Total</span>
                  <div className="stat-card-icon stat-icon-primary">
                    <CircleDollarSign size={16} />
                  </div>
                </div>
                <div className="stat-card-value">R$ {formatBRL(receitaTotal)}</div>
                <span className="stat-card-badge badge-muted">
                  {eventosFiltrados.length} evento{eventosFiltrados.length !== 1 ? 's' : ''}
                </span>
              </div>

              <div className="stat-card">
                <div className="stat-card-header">
                  <span className="stat-card-label">Receita Recebida</span>
                  <div className="stat-card-icon stat-icon-success">
                    <TrendingUp size={16} />
                  </div>
                </div>
                <div className="stat-card-value">R$ {formatBRL(receitaRecebida)}</div>
                <span className={`stat-card-badge ${receitaRecebida > 0 ? 'badge-success' : 'badge-muted'}`}>
                  {receitaTotal > 0 ? `${((receitaRecebida / receitaTotal) * 100).toFixed(0)}% do total` : 'Sem receita'}
                </span>
              </div>

              <div className="stat-card">
                <div className="stat-card-header">
                  <span className="stat-card-label">Sinais Pendentes</span>
                  <div className="stat-card-icon stat-icon-warning">
                    <Clock size={16} />
                  </div>
                </div>
                <div className="stat-card-value">R$ {formatBRL(valorSinaisPendentes)}</div>
                <span className="stat-card-badge badge-muted">
                  {sinaisPendentes.length} evento{sinaisPendentes.length !== 1 ? 's' : ''}
                </span>
              </div>

              <div className="stat-card">
                <div className="stat-card-header">
                  <span className="stat-card-label">A Receber</span>
                  <div className="stat-card-icon stat-icon-warning">
                    <TrendingDown size={16} />
                  </div>
                </div>
                <div className="stat-card-value">R$ {formatBRL(valorRestantesPendentes)}</div>
                <span className="stat-card-badge badge-muted">
                  {pagamentosPendentes.length} evento{pagamentosPendentes.length !== 1 ? 's' : ''} sem quitação
                </span>
              </div>

              <div className="stat-card">
                <div className="stat-card-header">
                  <span className="stat-card-label">Custo Total</span>
                  <div className="stat-card-icon stat-icon-danger">
                    <Minus size={16} />
                  </div>
                </div>
                <div className="stat-card-value">R$ {formatBRL(custoTotalPeriodo)}</div>
                <span className="stat-card-badge badge-muted">
                  estimado pelo período
                </span>
              </div>

              <div className="stat-card">
                <div className="stat-card-header">
                  <span className="stat-card-label">Lucro</span>
                  <div className={`stat-card-icon ${lucroTotal >= 0 ? 'stat-icon-success' : 'stat-icon-danger'}`}>
                    <TrendingUp size={16} />
                  </div>
                </div>
                <div className="stat-card-value">R$ {formatBRL(lucroTotal)}</div>
                <span className={`stat-card-badge ${lucroTotal >= 0 ? 'badge-success' : 'badge-danger'}`}>
                  {receitaRecebida > 0 ? `${margem.toFixed(0)}% de margem` : 'Sem receita recebida'}
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {breakdownServico.length > 0 && (
              <div className="card card-flush">
                <div className="card-section-header">
                  <h2 className="card-section-title">Receita por Tipo de Serviço</h2>
                </div>
                <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>Tipo de Serviço</th>
                      <th>Eventos</th>
                      <th>Receita Total</th>
                      <th>Ticket Médio</th>
                      <th>Custo Médio</th>
                      <th>Margem %</th>
                      <th>% do Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {breakdownServico.map(item => {
                      const margemItem = item.ticketMedio > 0
                        ? ((item.ticketMedio - item.custoMedio) / item.ticketMedio * 100)
                        : 0
                      return (
                        <tr key={item.tipo}>
                          <td><strong>{item.label}</strong></td>
                          <td>{item.count}</td>
                          <td><strong>R$ {formatBRL(item.receitaTotal)}</strong></td>
                          <td>R$ {formatBRL(item.ticketMedio)}</td>
                          <td>R$ {formatBRL(item.custoMedio)}</td>
                          <td>
                            <span className={`stat-card-badge ${margemItem >= 0 ? 'badge-success' : 'badge-danger'}`}>
                              {margemItem.toFixed(0)}%
                            </span>
                          </td>
                          <td>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                              <div style={{ flex: 1, background: 'var(--border)', borderRadius: '4px', height: '6px', minWidth: '60px' }}>
                                <div style={{ width: `${Math.max(item.percentual, 0).toFixed(0)}%`, background: 'var(--primary)', height: '6px', borderRadius: '4px' }} />
                              </div>
                              <span>{item.percentual.toFixed(0)}%</span>
                            </div>
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
                </div>
              </div>
            )}

            <div className="card card-flush">
              <div className="card-section-header">
                <h2 className="card-section-title">Pagamentos Pendentes</h2>
              </div>
              {pagamentosPendentes.length === 0 ? (
                <div className="empty-state">
                  <AlertCircle />
                  <p>Nenhum pagamento pendente no período.</p>
                </div>
              ) : (
                <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>Cliente</th>
                      <th>Data</th>
                      <th>Valor Total</th>
                      <th>Sinal</th>
                      <th>Pagamento Final</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pagamentosPendentes.map(evento => (
                      <tr key={evento.id}>
                        <td>
                          <button
                            className="btn btn-ghost btn-link-primary"
                            style={{ padding: 0, fontWeight: 600 }}
                            onClick={() => navigate(`/eventos/${evento.id}`)}
                          >
                            {evento.client?.nome || 'Cliente'}
                          </button>
                        </td>
                        <td>{formatDateBR(evento.data) || '-'}</td>
                        <td><strong>R$ {formatBRL(evento.valor)}</strong></td>
                        <td>
                          {evento.sinalRecebido ? (
                            <span className="status-badge status-assinado">
                              {evento.valorSinal ? `R$ ${formatBRL(evento.valorSinal)} recebido` : 'Recebido'}
                            </span>
                          ) : evento.valorSinal ? (
                            <button
                              className="btn btn-secondary"
                              style={{ fontSize: '12px', padding: '4px 10px' }}
                              onClick={() => handleMarkSinal(evento)}
                              disabled={markingId === `sinal-${evento.id}`}
                            >
                              {markingId === `sinal-${evento.id}`
                                ? <span className="spinner spinner-sm" />
                                : `Receber R$ ${formatBRL(evento.valorSinal)}`}
                            </button>
                          ) : (
                            <span className="status-badge status-sem-contrato">Sem sinal</span>
                          )}
                        </td>
                        <td>
                          {evento.restanteRecebido ? (
                            <span className="status-badge status-assinado">Recebido</span>
                          ) : (
                            <button
                              className="btn btn-secondary"
                              style={{ fontSize: '12px', padding: '4px 10px' }}
                              onClick={() => handleMarkRestante(evento)}
                              disabled={markingId === `restante-${evento.id}`}
                            >
                              {markingId === `restante-${evento.id}`
                                ? <span className="spinner spinner-sm" />
                                : `Receber R$ ${formatBRL(Math.max((Number(evento.valor) || 0) - (evento.sinalRecebido ? (Number(evento.valorSinal) || 0) : 0), 0))}`}
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                </div>
              )}
            </div>

            <div className="card card-flush">
              <div className="card-section-header">
                <h2 className="card-section-title">Histórico de Recebimentos</h2>
              </div>
              {pagamentosRecebidos.length === 0 ? (
                <div className="empty-state">
                  <AlertCircle />
                  <p>Nenhum pagamento recebido no período.</p>
                </div>
              ) : (
                <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>Cliente</th>
                      <th>Tipo</th>
                      <th>Data Evento</th>
                      <th>Valor Total</th>
                      <th>Recebido</th>
                      <th>Custo</th>
                      <th>Lucro</th>
                      <th>Sinal em</th>
                      <th>Final em</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pagamentosRecebidos.map(evento => {
                      const sinalPart = evento.sinalRecebido ? (Number(evento.valorSinal) || 0) : 0
                      const restantePart = evento.restanteRecebido ? Math.max((Number(evento.valor) || 0) - (Number(evento.valorSinal) || 0), 0) : 0
                      const totalRecebido = sinalPart + restantePart
                      const custo = effectiveCost(evento)
                      const lucro = totalRecebido - custo
                      const quitado = evento.restanteRecebido
                      return (
                        <tr key={evento.id}>
                          <td>
                            <button
                              className="btn btn-ghost btn-link-primary"
                              style={{ padding: 0, fontWeight: 600 }}
                              onClick={() => navigate(`/eventos/${evento.id}`)}
                            >
                              {evento.client?.nome || 'Cliente'}
                            </button>
                          </td>
                          <td>{evento.tipoEvento || '-'}</td>
                          <td>{formatDateBR(evento.data) || '-'}</td>
                          <td><strong>R$ {formatBRL(evento.valor)}</strong></td>
                          <td><strong>R$ {formatBRL(totalRecebido)}</strong></td>
                          <td>R$ {formatBRL(custo)}</td>
                          <td>
                            <span className={lucro >= 0 ? '' : 'stat-card-badge badge-danger'} style={{ fontWeight: 600, color: lucro >= 0 ? 'var(--success)' : undefined }}>
                              R$ {formatBRL(lucro)}
                            </span>
                          </td>
                          <td>{evento.dataRecebimentoSinal ? formatDateBR(evento.dataRecebimentoSinal) : '-'}</td>
                          <td>{evento.dataRecebimentoRestante ? formatDateBR(evento.dataRecebimentoRestante) : '-'}</td>
                          <td>
                            <span className={`status-badge ${quitado ? 'status-assinado' : 'status-pendente'}`}>
                              {quitado ? 'Quitado' : 'Parcialmente pago'}
                            </span>
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
                </div>
              )}
            </div>
            </div>
          </>
        )}
      </div>
      {toast && <div className={`toast ${toast.type}`}>{toast.msg}</div>}
    </>
  )
}
