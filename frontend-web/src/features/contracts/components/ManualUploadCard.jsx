import { Upload } from 'lucide-react'

export default function ManualUploadCard({
  disabled,
  manualFile,
  manualUploadSigned,
  onFileChange,
  onSignedChange,
}) {
  return (
    <div className="manual-upload-card">
      <div>
        <div className="form-label">Upload manual</div>
        <strong>Usar um PDF pronto para este evento</strong>
        <p>Escolha se esse PDF ainda precisa ir para assinatura ou se já é a via assinada.</p>
      </div>
      <div className="manual-upload-actions">
        <div className="manual-upload-status">
          <button
            type="button"
            className={!manualUploadSigned ? 'active' : ''}
            onClick={() => onSignedChange(false)}
            disabled={disabled}
          >
            Ainda não assinado
          </button>
          <button
            type="button"
            className={manualUploadSigned ? 'active' : ''}
            onClick={() => onSignedChange(true)}
            disabled={disabled}
          >
            Já assinado
          </button>
        </div>
        <label className="manual-upload-dropzone">
          <Upload size={18} />
          <span>{manualFile?.name || 'Selecionar PDF'}</span>
          <input
            type="file"
            accept="application/pdf,.pdf"
            disabled={disabled}
            onChange={e => onFileChange(e.target.files?.[0] || null)}
          />
        </label>
      </div>
    </div>
  )
}
