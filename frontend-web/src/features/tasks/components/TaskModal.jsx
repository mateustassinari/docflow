import { CheckSquare, Paperclip, Square, Trash2, X } from 'lucide-react'
import { formatDateBR } from '../../../utils/date'
import { tasksService } from '../services/tasksService'
import { SERVICE_OPTIONS, STATUS_OPTIONS } from '../hooks/useTasksPage'

function statusLabel(value) {
  return STATUS_OPTIONS.find(status => status.value === value)?.label || value
}

function eventLabel(event) {
  if (!event?.clienteNome) return ''
  const date = formatDateBR(event.data)
  return date ? `${event.clienteNome} - ${date}` : event.clienteNome
}

export default function TaskModal({
  addChecklistItem,
  addComment,
  commentText,
  deleteAttachment,
  deleteComment,
  events,
  form,
  modalOpen,
  onDeleteTask,
  saveTask,
  saving,
  selectedTask,
  setCommentText,
  setField,
  setModalOpen,
  updateChecklistItem,
  removeChecklistItem,
  uploadAttachment,
}) {
  if (!modalOpen) return null

  return (
    <div className="modal-overlay">
      <form className="event-edit-modal task-modal" onSubmit={saveTask}>
        <div className="pdf-modal-header">
          <div>
            <strong>{selectedTask ? 'Editar tarefa' : 'Nova tarefa'}</strong>
            <span>{selectedTask ? statusLabel(selectedTask.status) : 'Organize prazo, checklist e anexos'}</span>
          </div>
          <button type="button" className="topbar-icon-btn" onClick={() => setModalOpen(false)} title="Fechar">
            <X size={16} />
          </button>
        </div>

        <div className="event-edit-body task-modal-body">
          <div className="form-grid-2">
            <label className="form-group full">
              <span className="form-label">Título</span>
              <input className="form-input" value={form.titulo} onChange={event => setField('titulo', event.target.value)} required />
            </label>
            <label className="form-group">
              <span className="form-label">Evento vinculado</span>
              <select className="form-select" value={form.eventId} onChange={event => setField('eventId', event.target.value)}>
                <option value="">Tarefa avulsa</option>
                {events.map(evento => (
                  <option key={evento.id} value={evento.id}>{eventLabel({ clienteNome: evento.client?.nome, data: evento.data }) || `Evento ${evento.id}`}</option>
                ))}
              </select>
            </label>
            <label className="form-group">
              <span className="form-label">Serviço</span>
              <select className="form-select" value={form.tipoServico} onChange={event => setField('tipoServico', event.target.value)}>
                <option value="">Geral</option>
                {SERVICE_OPTIONS.map(service => <option key={service.value} value={service.value}>{service.label}</option>)}
              </select>
            </label>
            <label className="form-group">
              <span className="form-label">Início</span>
              <input className="form-input" type="date" value={form.prazoData} onChange={event => setField('prazoData', event.target.value)} required />
            </label>
            <label className="form-group">
              <span className="form-label">Fim</span>
              <input className="form-input" type="date" min={form.prazoData || undefined} value={form.prazoDataFim} onChange={event => setField('prazoDataFim', event.target.value)} />
            </label>
            <label className="form-group">
              <span className="form-label">Horário início</span>
              <input className="form-input" type="time" value={form.prazoHorario} onChange={event => setField('prazoHorario', event.target.value)} required />
            </label>
            <label className="form-group">
              <span className="form-label">Horário fim</span>
              <input className="form-input" type="time" value={form.prazoHorarioFim} onChange={event => setField('prazoHorarioFim', event.target.value)} />
            </label>
            <label className="form-group">
              <span className="form-label">Status</span>
              <select className="form-select" value={form.status} onChange={event => setField('status', event.target.value)}>
                {STATUS_OPTIONS.map(status => <option key={status.value} value={status.value}>{status.label}</option>)}
              </select>
            </label>
            <label className="form-group full">
              <span className="form-label">Etiquetas</span>
              <input className="form-input" value={form.etiquetasText} onChange={event => setField('etiquetasText', event.target.value)} placeholder="urgente, fornecedor, cliente" />
            </label>
            <label className="form-group full">
              <span className="form-label">Descrição</span>
              <textarea className="form-textarea" rows={3} value={form.descricao} onChange={event => setField('descricao', event.target.value)} />
            </label>
          </div>

          <div className="task-section">
            <div className="task-section-header">
              <strong>Checklist</strong>
              <button type="button" className="btn btn-secondary" onClick={addChecklistItem}>Adicionar item</button>
            </div>
            <div className="task-checklist-editor">
              {form.checklistItems.map((item, index) => (
                <div key={index} className="task-checklist-row">
                  <button type="button" className={`action-btn ${item.concluido ? 'action-btn-success' : ''}`} onClick={() => updateChecklistItem(index, { concluido: !item.concluido })}>
                    {item.concluido ? <CheckSquare size={14} /> : <Square size={14} />}
                  </button>
                  <input className="form-input" value={item.texto} onChange={event => updateChecklistItem(index, { texto: event.target.value })} placeholder="Item do checklist" />
                  <button type="button" className="action-btn action-btn-danger" onClick={() => removeChecklistItem(index)} title="Remover item">
                    <Trash2 size={14} />
                  </button>
                </div>
              ))}
              {form.checklistItems.length === 0 && <p className="text-secondary">Nenhum item de checklist.</p>}
            </div>
          </div>

          {selectedTask && (
            <>
              <div className="task-section">
                <div className="task-section-header"><strong>Comentários</strong></div>
                <div className="task-comment-list">
                  {(selectedTask.comments || []).map(comment => (
                    <div key={comment.id} className="task-comment">
                      <div>
                        <p>{comment.texto}</p>
                        <span>{formatDateBR(comment.createdAt)}</span>
                      </div>
                      <button type="button" className="action-btn action-btn-danger" onClick={() => deleteComment(comment.id)} title="Remover comentário">
                        <Trash2 size={14} />
                      </button>
                    </div>
                  ))}
                </div>
                <div className="task-comment-form">
                  <input className="form-input" value={commentText} onChange={event => setCommentText(event.target.value)} placeholder="Adicionar comentário" />
                  <button type="button" className="btn btn-secondary" onClick={addComment}>Comentar</button>
                </div>
              </div>

              <div className="task-section">
                <div className="task-section-header">
                  <strong>Anexos</strong>
                  <label className="btn btn-secondary task-upload-btn">
                    <Paperclip size={14} /> Anexar
                    <input type="file" onChange={event => uploadAttachment(event.target.files?.[0])} />
                  </label>
                </div>
                <div className="task-attachment-list">
                  {(selectedTask.attachments || []).map(attachment => (
                    <div key={attachment.id} className="task-attachment">
                      <a href={tasksService.attachmentUrl(selectedTask.id, attachment.id)} target="_blank" rel="noreferrer">{attachment.originalName}</a>
                      <button type="button" className="action-btn action-btn-danger" onClick={() => deleteAttachment(attachment.id)} title="Remover anexo">
                        <Trash2 size={14} />
                      </button>
                    </div>
                  ))}
                  {(selectedTask.attachments || []).length === 0 && <p className="text-secondary">Nenhum anexo.</p>}
                </div>
              </div>
            </>
          )}
        </div>

        <div className="form-footer signature-modal-footer">
          {selectedTask && (
            <button type="button" className="btn btn-danger" onClick={() => onDeleteTask(selectedTask)}>
              Excluir tarefa
            </button>
          )}
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Salvando...' : 'Salvar tarefa'}
          </button>
        </div>
      </form>
    </div>
  )
}
