import TopBar from '../../../layouts/TopBar'
import SortableTh from '../../../shared/components/SortableTh'
import ConfirmDialog from '../../../shared/components/ConfirmDialog'
import { Plus, AlertCircle, X, Search, Phone, Mail, FileText, Edit2, Trash2 } from 'lucide-react'
import { useClientsPage } from '../hooks/useClientsPage'

function getInitials(nome) {
  return (nome || '?').split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase()
}

function getColorClass(str) {
  let hash = 0
  for (let c of (str || '')) hash = c.charCodeAt(0) + ((hash << 5) - hash)
  return `avatar-color-${Math.abs(hash) % 5}`
}

export default function Clientes() {
  const {
    deleteConfirmId,
    editingId,
    filtered,
    form,
    handleDelete,
    handleEdit,
    handleSort,
    handleSubmit,
    loading,
    nextPage,
    openNewClientModal,
    pageStart,
    paginated,
    previousPage,
    safePage,
    saving,
    searchTerm,
    setCpfCnpj,
    setCurrentPage,
    setDeleteConfirmId,
    setEmail,
    setNome,
    setSearchTerm,
    setShowModal,
    setTelefone,
    showModal,
    sortConfig,
    toast,
    totalPages,
  } = useClientsPage()

  return (
    <>
      <TopBar title="Clientes" />
      <div className="page-content">
        <div className="page-header">
          <div className="page-header-left">
            <h1>Clientes</h1>
            <p>Gerencie todos os clientes cadastrados na plataforma.</p>
          </div>
          <button className="btn btn-primary" onClick={openNewClientModal}>
            <Plus size={16} />
            Novo Cliente
          </button>
        </div>

        <div className="table-toolbar">
          <div className="search-bar">
            <Search size={15} />
            <input
              type="text"
              placeholder="Buscar por nome ou e-mail..."
              value={searchTerm}
              onChange={event => setSearchTerm(event.target.value)}
              autoComplete="off"
            />
          </div>
        </div>

        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <SortableTh sortKey="nome" sortConfig={sortConfig} onSort={handleSort}>Cliente</SortableTh>
                <SortableTh sortKey="contato" sortConfig={sortConfig} onSort={handleSort}>Contato</SortableTh>
                <SortableTh sortKey="documento" sortConfig={sortConfig} onSort={handleSort}>Documento</SortableTh>
                <SortableTh sortKey="id" sortConfig={sortConfig} onSort={handleSort}>ID do Sistema</SortableTh>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={5} className="table-loading-cell">
                    <span className="spinner spinner-table" />
                  </td>
                </tr>
              ) : filtered.length > 0 ? (
                paginated.map(client => (
                  <tr key={client.id}>
                    <td>
                      <div className="client-info">
                        <div className={`client-avatar ${getColorClass(client.nome)}`}>
                          {getInitials(client.nome)}
                        </div>
                        <div className="client-info-text">
                          <strong>{client.nome}</strong>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div className="contact-list">
                        {client.email && <div className="contact-row"><Mail size={12} /> {client.email}</div>}
                        {client.telefone && <div className="contact-row"><Phone size={12} /> {client.telefone}</div>}
                        {!client.email && !client.telefone && <span className="text-muted">—</span>}
                      </div>
                    </td>
                    <td>
                      <div className="contact-row">
                        <FileText size={12} /> {client.cpfCnpj || '—'}
                      </div>
                    </td>
                    <td>
                      <span className="status-badge system-id-badge">
                        CLI-{client.id}
                      </span>
                    </td>
                    <td>
                      <div className="action-btns">
                        <button className="action-btn" title="Editar cliente" onClick={() => handleEdit(client)}>
                          <Edit2 size={14} />
                        </button>
                        <button className="action-btn" title="Excluir cliente" onClick={() => setDeleteConfirmId(client.id)}>
                          <Trash2 size={14} color="var(--danger)" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5}>
                    <div className="empty-state">
                      <AlertCircle />
                      <h3>Nenhum cliente encontrado</h3>
                      <p>Você ainda não tem clientes cadastrados ou a busca não retornou resultados.</p>
                      <button className="btn btn-secondary mt-12" onClick={openNewClientModal}>
                        Cadastrar primeiro cliente
                      </button>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          <div className="pagination">
            <span>
              Mostrando {filtered.length > 0 ? pageStart + 1 : 0}-{Math.min(pageStart + 8, filtered.length)} de {filtered.length} clientes
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

      {showModal && (
        <div className="modal-overlay">
          <div className="card modal-card-sm">
            <div className="modal-header">
              <h2 className="modal-title">{editingId ? 'Editar Cliente' : 'Cadastrar Cliente'}</h2>
              <button className="btn btn-ghost modal-close-btn" onClick={() => setShowModal(false)}><X size={18} /></button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="form-group full">
                  <label className="form-label">Nome Completo</label>
                  <input className="form-input" placeholder="Ex: Roberto Carlos" value={form.nome} onChange={setNome} required />
                </div>

                <div className="form-group full">
                  <label className="form-label">CPF / CNPJ</label>
                  <input className="form-input" placeholder="000.000.000-00" value={form.cpfCnpj} onChange={setCpfCnpj} required />
                </div>

                <div className="form-grid-2">
                  <div className="form-group">
                    <label className="form-label">E-mail</label>
                    <input type="email" className="form-input" placeholder="roberto@email.com" value={form.email} onChange={setEmail} required />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Telefone</label>
                    <input className="form-input" placeholder="(11) 99999-9999" value={form.telefone} onChange={setTelefone} required />
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? <span className="spinner" /> : null}
                  {saving ? 'Salvando...' : 'Salvar Cliente'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={Boolean(deleteConfirmId)}
        title="Excluir cliente?"
        description="Essa ação remove o cliente do cadastro. Se ele estiver vinculado a eventos, o backend pode bloquear a exclusão para preservar o histórico."
        confirmLabel="Excluir cliente"
        tone="danger"
        onCancel={() => setDeleteConfirmId(null)}
        onConfirm={() => handleDelete(deleteConfirmId)}
      />

      {toast && (
        <div className={`toast ${toast.type}`}>
          {toast.msg}
        </div>
      )}
    </>
  )
}
