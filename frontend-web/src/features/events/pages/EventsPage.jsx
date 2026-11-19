import { useNavigate } from 'react-router-dom'
import TopBar from '../../../layouts/TopBar'
import SortableTh from '../../../shared/components/SortableTh'
import { Plus, Eye, FileText, AlertCircle, Send, X } from 'lucide-react'
import { formatDateBR } from '../../../utils/date'
import { EVENT_STATUS_MAP, useEventsPage } from '../hooks/useEventsPage'

const TIPO_CLASSES = { Corporativo: 'tipo-corporativo', Social: 'tipo-social', Conferência: 'tipo-conferencia', Casamento: 'tipo-casamento' }
const PAGE_SIZE = 8

const QUICK_FILTERS = [
  { key: 'todos', label: 'Todos' },
  { key: 'sem-contrato', label: 'Sem contrato' },
  { key: 'dispensados', label: 'Dispensados' },
  { key: 'gerados', label: 'Gerados' },
  { key: 'assinatura', label: 'Pendente assinatura' },
  { key: 'assinados', label: 'Assinados' },
  { key: 'proximos', label: 'Próximos 7 dias' },
  { key: 'proximos15', label: 'Próximos 15 dias' },
]

function getInitials(nome) {
  return (nome || '?').split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase()
}

function getColorClass(str) {
  let hash = 0
  for (let c of (str || '')) hash = c.charCodeAt(0) + ((hash << 5) - hash)
  return `avatar-color-${Math.abs(hash) % 5}`
}

function PaymentBadge({ event }) {
  if (event.restanteRecebido) {
    return <span className="status-badge status-assinado">Pago</span>
  }
  if (event.sinalRecebido) {
    return <span className="status-badge status-rascunho">Aguardando restante</span>
  }
  if (!event.valorSinal) {
    return <span className="status-badge status-sem-contrato">Sem sinal</span>
  }
  return <span className="status-badge status-pendente">Sinal pendente</span>
}

export default function Eventos() {
  const navigate = useNavigate()
  const {
    additionalSigners,
    addAdditionalSigner,
    filter,
    filtered,
    handleSendContract,
    handleSort,
    nextPage,
    openSendModal,
    pageStart,
    paginated,
    previousPage,
    removeAdditionalSigner,
    safePage,
    searchTerm,
    sendTarget,
    sendingId,
    setCurrentPage,
    setFilter,
    setSearchTerm,
    setSendTarget,
    sortConfig,
    toast,
    totalPages,
    updateAdditionalSigner,
  } = useEventsPage()

  return (
    <>
      <TopBar
        breadcrumb={[{ label: 'Dashboard', href: '/' }]}
        title="Eventos"
        searchValue={searchTerm}
        onSearchChange={setSearchTerm}
      />
      <div className="page-content">
        <div className="page-header">
          <div className="page-header-left">
            <h1>Eventos</h1>
            <p>Gerencie todos os eventos e contratos logísticos da plataforma.</p>
          </div>
          <button className="btn btn-primary" onClick={() => navigate('/eventos/novo')}>
            <Plus size={16} />
            Novo Evento
          </button>
        </div>

        <div className="table-toolbar">
          <div className="toolbar-inline">
            <div className="tabs tabs-inline">
              {QUICK_FILTERS.map(tab => (
                <button key={tab.key} className={`tab-btn ${filter === tab.key ? 'active' : ''}`} onClick={() => setFilter(tab.key)}>
                  {tab.label}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <SortableTh sortKey="cliente" sortConfig={sortConfig} onSort={handleSort}>Cliente</SortableTh>
                <SortableTh sortKey="tipo" sortConfig={sortConfig} onSort={handleSort}>Tipo</SortableTh>
                <SortableTh sortKey="data" sortConfig={sortConfig} onSort={handleSort}>Data</SortableTh>
                <SortableTh sortKey="valor" sortConfig={sortConfig} onSort={handleSort}>Valor</SortableTh>
                <SortableTh sortKey="contrato" sortConfig={sortConfig} onSort={handleSort}>Contrato</SortableTh>
                <th>Pagamento</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {paginated.map((event, index) => {
                const nome = event.client?.nome || 'Cliente'
                const tipo = event.tipoEvento || '-'
                const valor = event.valor || 0
                const status = (event.status || '').toUpperCase()
                const statusInfo = EVENT_STATUS_MAP[status] || EVENT_STATUS_MAP['']
                return (
                  <tr key={event.id || index}>
                    <td>
                      <div className="client-info">
                        <div className={`client-avatar ${getColorClass(nome)}`}>
                          {getInitials(nome)}
                        </div>
                        <div className="client-info-text">
                          <strong>{nome}</strong>
                          <span>ID: {event.ref || `REV-${event.id || index}`}</span>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className={`tipo-chip ${TIPO_CLASSES[tipo] || 'tipo-corporativo'}`}>{tipo}</span>
                    </td>
                    <td>{formatDateBR(event.data) || '-'}</td>
                    <td>
                      <strong>R$ {Number(valor).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong>
                    </td>
                    <td>
                      <span className={`status-badge ${statusInfo.cls}`}>{statusInfo.label}</span>
                    </td>
                    <td>
                      <PaymentBadge event={event} />
                    </td>
                    <td>
                      <div className="action-btns">
                        <button className="action-btn" title="Ver detalhes" onClick={() => navigate(`/eventos/${event.id}`)}>
                          <Eye size={14} />
                        </button>
                        {!event.contract && !event.contratoDispensado && (
                          <button className="action-btn" title="Gerar contrato" onClick={() => navigate(`/contratos/gerar?eventId=${event.id}`)}>
                            <FileText size={14} />
                          </button>
                        )}
                        {event.contract?.status === 'RASCUNHO' && (
                          <button
                            className="action-btn"
                            title="Enviar para assinatura"
                            onClick={() => openSendModal(event, event.contract)}
                            disabled={sendingId === event.contract.id}
                          >
                            {sendingId === event.contract.id ? <span className="spinner spinner-sm" /> : <Send size={14} />}
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                )
              })}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={7}>
                    <div className="empty-state">
                      <AlertCircle />
                      <h3>Nenhum evento encontrado</h3>
                      <p>Tente mudar o filtro ou crie um novo evento.</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          <div className="pagination">
            <span>
              Mostrando {filtered.length > 0 ? pageStart + 1 : 0}-{Math.min(pageStart + PAGE_SIZE, filtered.length)} de {filtered.length} eventos
            </span>
            <div className="pagination-pages">
              <button className="page-btn" onClick={previousPage} disabled={safePage === 1}>
                ‹
              </button>
              {Array.from({ length: totalPages }, (_, index) => index + 1).map(page => (
                <button
                  key={page}
                  className={`page-btn ${safePage === page ? 'active' : ''}`}
                  onClick={() => setCurrentPage(page)}
                >
                  {page}
                </button>
              ))}
              <button className="page-btn" onClick={nextPage} disabled={safePage === totalPages}>
                ›
              </button>
            </div>
          </div>
        </div>
      </div>

      {sendTarget && (
        <div className="modal-overlay">
          <div className="event-edit-modal signature-modal">
            <div className="pdf-modal-header">
              <div>
                <strong>Enviar para assinatura</strong>
                <span>Adicione signatários extras se outras pessoas também precisarem assinar.</span>
              </div>
              <button type="button" className="topbar-icon-btn" onClick={() => setSendTarget(null)} title="Fechar envio">
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
                        <input className="form-input" value={signer.nome} onChange={event => updateAdditionalSigner(index, 'nome', event.target.value)} placeholder="Nome do signatário" />
                      </label>
                      <label className="form-group">
                        <span className="form-label">E-mail</span>
                        <input className="form-input" type="email" value={signer.email} onChange={event => updateAdditionalSigner(index, 'email', event.target.value)} placeholder="email@exemplo.com" />
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
              <button type="button" className="btn btn-secondary" onClick={() => setSendTarget(null)}>Cancelar</button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => handleSendContract(sendTarget.event, sendTarget.contract)}
                disabled={sendingId === sendTarget.contract.id}
              >
                <Send size={15} /> {sendingId === sendTarget.contract.id ? 'Enviando...' : 'Enviar para assinatura'}
              </button>
            </div>
          </div>
        </div>
      )}
      {toast && <div className={`toast ${toast.type}`}>{toast.msg}</div>}
    </>
  )
}
