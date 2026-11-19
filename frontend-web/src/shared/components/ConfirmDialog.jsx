import { AlertTriangle, X } from 'lucide-react'

export default function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  tone = 'warning',
  loading = false,
  onCancel,
  onConfirm,
}) {
  if (!open) return null

  return (
    <div className="modal-overlay">
      <div className="confirm-dialog">
        <div className={`confirm-dialog-icon ${tone}`}>
          <AlertTriangle size={20} />
        </div>
        <button type="button" className="confirm-dialog-close" onClick={onCancel} disabled={loading} title="Fechar">
          <X size={16} />
        </button>
        <div className="confirm-dialog-content">
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
        <div className="confirm-dialog-actions">
          <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={loading}>
            {cancelLabel}
          </button>
          <button type="button" className={`btn ${tone === 'danger' ? 'btn-danger' : 'btn-primary'}`} onClick={onConfirm} disabled={loading}>
            {loading ? <span className="spinner" /> : null}
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
