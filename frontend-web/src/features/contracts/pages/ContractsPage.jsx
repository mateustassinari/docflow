import { useNavigate } from 'react-router-dom'
import TopBar from '../../../layouts/TopBar'
import SortableTh from '../../../shared/components/SortableTh'
import { AlertCircle, Eye, FileText, Plus, Send } from 'lucide-react'
import { CONTRACT_STATUS_MAP, useContractsPage } from '../hooks/useContractsPage'

export default function Contratos() {
  const navigate = useNavigate()
  const {
    contratos,
    handleSend,
    handleSort,
    loading,
    sendingId,
    sortConfig,
    sortedContratos,
    toast,
  } = useContractsPage()

  return (
    <>
      <TopBar title="Contratos" />
      <div className="page-content">
        <div className="page-header">
          <div className="page-header-left">
            <h1>Contratos</h1>
            <p>Visualize e gerencie todos os contratos gerados.</p>
          </div>
          <button className="btn btn-primary" onClick={() => navigate('/contratos/gerar')}>
            <Plus size={16} />
            Gerar contrato
          </button>
        </div>

        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <SortableTh sortKey="cliente" sortConfig={sortConfig} onSort={handleSort}>Cliente</SortableTh>
                <SortableTh sortKey="evento" sortConfig={sortConfig} onSort={handleSort}>Evento</SortableTh>
                <SortableTh sortKey="template" sortConfig={sortConfig} onSort={handleSort}>Template</SortableTh>
                <SortableTh sortKey="status" sortConfig={sortConfig} onSort={handleSort}>Status</SortableTh>
                <SortableTh sortKey="pdf" sortConfig={sortConfig} onSort={handleSort}>PDF</SortableTh>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6} className="table-loading-cell"><span className="spinner spinner-table" /></td></tr>
              ) : contratos.length > 0 ? (
                sortedContratos.map(contract => {
                  const statusInfo = CONTRACT_STATUS_MAP[contract.status] || CONTRACT_STATUS_MAP.RASCUNHO
                  return (
                    <tr key={contract.id}>
                      <td>{contract.event?.client?.nome || 'Cliente'}</td>
                      <td>{contract.event?.tipoEvento || '-'}</td>
                      <td>{contract.template?.nome || '-'}</td>
                      <td><span className={`status-badge ${statusInfo.cls}`}>{statusInfo.label}</span></td>
                      <td>
                        {contract.pdfUrl
                          ? <span className="inline-icon-text"><FileText size={13} /> Gerado</span>
                          : <span className="text-muted">Pendente</span>}
                      </td>
                      <td>
                        <div className="action-btns">
                          <button className="action-btn" title="Ver evento" onClick={() => navigate(`/eventos/${contract.event?.id}`)}>
                            <Eye size={14} />
                          </button>
                          <button className="action-btn" title="Enviar para assinatura" onClick={() => handleSend(contract.id)} disabled={sendingId === contract.id || contract.status !== 'RASCUNHO'}>
                            <Send size={14} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })
              ) : (
                <tr>
                  <td colSpan={6}>
                    <div className="empty-state">
                      <AlertCircle />
                      <h3>Nenhum contrato encontrado</h3>
                      <p>Gere um contrato a partir de um evento cadastrado.</p>
                      <button className="btn btn-secondary" onClick={() => navigate('/contratos/gerar')}>Gerar primeiro contrato</button>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
      {toast && <div className={`toast ${toast.type}`}>{toast.msg}</div>}
    </>
  )
}
