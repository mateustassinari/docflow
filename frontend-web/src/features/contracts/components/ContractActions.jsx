import { FileText, Send, Upload } from 'lucide-react'

export default function ContractActions({
  canUploadManual,
  generatedContract,
  saving,
  uploading,
  sending,
  canRunPrimaryActions,
  onBack,
  onGenerate,
  onManualUpload,
  onResetGeneratedContract,
  onOpenSendModal,
}) {
  return (
    <div className="contract-actions-bar">
      <button className="btn btn-secondary" onClick={onBack}>
        Voltar
      </button>
      {generatedContract && (
        <button className="btn btn-secondary" onClick={onResetGeneratedContract} disabled={saving || uploading || sending}>
          Selecionar outro evento
        </button>
      )}
      {!generatedContract && (
        <button className="btn btn-secondary" onClick={onManualUpload} disabled={!canUploadManual}>
          {uploading ? <span className="spinner" /> : <Upload size={15} />}
          {uploading ? 'Enviando PDF...' : 'Upload manual'}
        </button>
      )}
      <button className="btn btn-secondary" onClick={onGenerate} disabled={!canRunPrimaryActions || Boolean(generatedContract)}>
        {saving ? <span className="spinner" /> : <FileText size={15} />}
        {saving ? 'Gerando...' : 'Gerar PDF'}
      </button>
      <button className="btn btn-primary" onClick={onOpenSendModal} disabled={saving || uploading || sending || !generatedContract?.id || generatedContract?.status === 'ASSINADO'}>
        {sending ? <span className="spinner" /> : <Send size={15} />}
        {generatedContract?.status === 'ASSINADO' ? 'Contrato já assinado' : sending ? 'Enviando...' : 'Enviar para assinatura'}
      </button>
    </div>
  )
}
