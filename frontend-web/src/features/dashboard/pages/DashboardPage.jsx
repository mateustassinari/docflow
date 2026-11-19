import TopBar from '../../../layouts/TopBar'
import SortableTh from '../../../shared/components/SortableTh'
import {
  Calendar,
  TrendingUp,
  Clock,
  Send,
  AlertCircle,
  ArrowRight,
} from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { formatDateBR } from '../../../utils/date'
import { useDashboardPage } from '../hooks/useDashboardPage'

const TIPO_CLASSES = { Corporativo: 'tipo-corporativo', Social: 'tipo-social', Conferencia: 'tipo-conferencia', Casamento: 'tipo-casamento' }

function getInitials(nome) {
  if (!nome) return '?'
  return nome.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase()
}

function getColorClass(str) {
  let hash = 0
  for (let c of str) hash = c.charCodeAt(0) + ((hash << 5) - hash)
  return `avatar-color-${Math.abs(hash) % 5}`
}

function normalizeType(tipo) {
  return String(tipo || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '')
}

function statusClass(status) {
  const map = {
    ASSINADO: 'status-assinado',
    ENVIADO: 'status-pendente',
    RASCUNHO: 'status-rascunho',
    DISPENSADO: 'status-sem-contrato',
    '': 'status-sem-contrato',
  }
  return `status-badge ${map[status] || 'status-sem-contrato'}`
}

function statusLabel(status) {
  const map = {
    ASSINADO: 'Assinado',
    ENVIADO: 'Pendente de assinatura',
    RASCUNHO: 'Gerado',
    DISPENSADO: 'Sem contrato necessário',
    '': 'Sem contrato',
  }
  return map[status] || status
}

export default function Dashboard() {
  const navigate = useNavigate()
  const {
    eventosMes,
    handleSort,
    pendentes,
    pendentesEnvio,
    receitaMedia,
    searchTerm,
    setSearchTerm,
    sortConfig,
    sortedVisibleEventos,
    totalReceita,
    visibleEventos,
  } = useDashboardPage()

  return (
    <>
      <TopBar
        title="Dashboard"
        searchValue={searchTerm}
        onSearchChange={setSearchTerm}
      />
      <div className="page-content">
        <div className="page-header">
          <div className="page-header-left">
            <h1>Dashboard</h1>
          </div>
        </div>

        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-card-header">
              <span className="stat-card-label">Eventos este mês</span>
              <div className="stat-card-icon stat-icon-primary">
                <Calendar size={16} />
              </div>
            </div>
            <div className="stat-card-value">{eventosMes}</div>
          </div>

          <div className="stat-card">
            <div className="stat-card-header">
              <span className="stat-card-label">Pendente de Envio</span>
              <div className="stat-card-icon stat-icon-primary">
                <Send size={16} />
              </div>
            </div>
            <div className="stat-card-value">{pendentesEnvio}</div>
          </div>

          <div className="stat-card">
            <div className="stat-card-header">
              <span className="stat-card-label">Aguardando Assinatura</span>
              <div className="stat-card-icon stat-icon-warning">
                <Clock size={16} />
              </div>
            </div>
            <div className="stat-card-value">{pendentes}</div>
          </div>

          <div className="stat-card">
            <div className="stat-card-header">
              <span className="stat-card-label">Receita Mensal</span>
              <div className="stat-card-icon stat-icon-success">
                <TrendingUp size={16} />
              </div>
            </div>
            <div className="stat-card-value">
              R$ {(totalReceita / 1000).toFixed(1)}k
            </div>
            <span className={`stat-card-badge ${totalReceita > 0 ? 'badge-success' : 'badge-muted'}`}>
              {totalReceita > 0 ? `Ticket médio R$ ${receitaMedia.toLocaleString('pt-BR', { maximumFractionDigits: 0 })}` : 'Sem receita registrada'}
            </span>
          </div>
        </div>

        <div className="card card-flush">
          <div className="card-section-header">
            <h2 className="card-section-title">Próximos Eventos</h2>
            <button className="btn btn-ghost btn-link-primary" onClick={() => navigate('/eventos')}>
              Ver todos <ArrowRight size={14} />
            </button>
          </div>

          <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <SortableTh sortKey="cliente" sortConfig={sortConfig} onSort={handleSort}>Cliente</SortableTh>
                <SortableTh sortKey="tipo" sortConfig={sortConfig} onSort={handleSort}>Tipo de Evento</SortableTh>
                <SortableTh sortKey="data" sortConfig={sortConfig} onSort={handleSort}>Data</SortableTh>
                <SortableTh sortKey="valor" sortConfig={sortConfig} onSort={handleSort}>Valor</SortableTh>
                <SortableTh sortKey="status" sortConfig={sortConfig} onSort={handleSort}>Status</SortableTh>
              </tr>
            </thead>
            <tbody>
              {sortedVisibleEventos.slice(0, 5).map((ev, i) => {
                const nome = ev.client?.nome || 'Cliente'
                const tipo = ev.tipoEvento || '-'
                const data = ev.data
                const valor = ev.valor || 0
                const status = (ev.status || '').toUpperCase()
                return (
                  <tr key={ev.id || i} className="clickable-row" onClick={() => navigate(`/eventos/${ev.id}`)}>
                    <td>
                      <div className="client-info">
                        <div className={`client-avatar ${getColorClass(nome)}`}>
                          {getInitials(nome)}
                        </div>
                        <div className="client-info-text">
                          <strong>{nome}</strong>
                        </div>
                      </div>
                    </td>
                    <td><span className={`tipo-chip ${TIPO_CLASSES[normalizeType(tipo)] || 'tipo-corporativo'}`}>{tipo}</span></td>
                    <td>{formatDateBR(data) || '-'}</td>
                    <td><strong>R$ {Number(valor).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong></td>
                    <td><span className={statusClass(status)}>{statusLabel(status)}</span></td>
                  </tr>
                )
              })}
              {visibleEventos.length === 0 && (
                <tr><td colSpan={5}><div className="empty-state"><AlertCircle />Nenhum evento encontrado</div></td></tr>
              )}
            </tbody>
          </table>
          </div>
        </div>
      </div>
    </>
  )
}
