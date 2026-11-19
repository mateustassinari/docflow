import { useNavigate, useSearchParams } from 'react-router-dom'
import TopBar from '../../../layouts/TopBar'
import { Send, X } from 'lucide-react'
import ContractActions from '../components/ContractActions'
import ContractDataPreview from '../components/ContractDataPreview'
import ContractPdfPreview from '../components/ContractPdfPreview'
import EventSelector from '../components/EventSelector'
import ManualUploadCard from '../components/ManualUploadCard'
import TemplateSelector from '../components/TemplateSelector'
import { useGenerateContractPage } from '../hooks/useGenerateContractPage'

export default function GenerateContractPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const initialEventId = searchParams.get('eventId') || ''

  const {
    additionalSigners,
    availableEventos,
    generatedContract,
    loading,
    manualFile,
    manualUploadSigned,
    saving,
    selectedEvent,
    selectedEventId,
    selectedTemplate,
    selectedTemplateId,
    sending,
    sendOpen,
    templates,
    toast,
    uploading,
    visibleTemplates,
    addAdditionalSigner,
    handleGenerate,
    handleManualUpload,
    handleResetGeneratedContract,
    handleSend,
    openSendModal,
    removeAdditionalSigner,
    selectEvent,
    selectTemplate,
    setManualFile,
    setManualUploadSigned,
    setSendOpen,
    updateAdditionalSigner,
  } = useGenerateContractPage(initialEventId)

  const handleSendAndNavigate = async () => {
    const contract = await handleSend()
    if (contract) {
      setTimeout(() => navigate(`/eventos/${contract.event?.id || selectedEventId}`), 1200)
    }
  }

  return (
    <>
      <TopBar breadcrumb={[{ label: 'Eventos', href: '/eventos' }]} title="Gerar Contrato" />
      <div className="page-content">
        <div className="page-header">
          <div className="page-header-left">
            <h1>Gerar contrato</h1>
            <p>Selecione o evento e o template cadastrado no backend.</p>
          </div>
        </div>

        {loading ? (
          <div className="empty-state card generate-contract-loading">
            <span className="spinner generate-contract-spinner" />
          </div>
        ) : (
          <div className="generate-contract-layout">
            <div className="generate-contract-sidebar">
              <EventSelector
                availableEventos={availableEventos}
                disabled={Boolean(generatedContract) || saving || uploading || sending}
                selectedEventId={selectedEventId}
                onSelect={selectEvent}
              />

              <TemplateSelector
                disabled={Boolean(generatedContract) || saving || uploading || sending}
                selectedTemplateId={selectedTemplateId}
                templates={templates}
                visibleTemplates={visibleTemplates}
                onSelect={selectTemplate}
              />
            </div>

            <div className="card generate-contract-card">
              <div className="generate-contract-card-header">
                <h2>Prévia dos dados</h2>
                {generatedContract && <span className={`status-badge status-${String(generatedContract.status || '').toLowerCase()}`}>{generatedContract.status}</span>}
              </div>

              <div className="generate-contract-card-body">
                <ContractDataPreview
                  selectedEvent={selectedEvent}
                  selectedTemplate={selectedTemplate}
                />

                {!generatedContract && (
                  <ManualUploadCard
                    disabled={saving || uploading || sending || Boolean(generatedContract)}
                    manualFile={manualFile}
                    manualUploadSigned={manualUploadSigned}
                    onFileChange={setManualFile}
                    onSignedChange={setManualUploadSigned}
                  />
                )}

                <ContractPdfPreview
                  contract={generatedContract}
                  onReviewData={() => navigate(`/eventos/${generatedContract.event?.id || selectedEventId}?edit=1`)}
                />
              </div>

              <ContractActions
                canUploadManual={Boolean(!saving && !uploading && !sending && selectedEventId && selectedTemplateId && manualFile)}
                canRunPrimaryActions={Boolean(!saving && !uploading && !sending && selectedEventId && selectedTemplateId)}
                generatedContract={generatedContract}
                saving={saving}
                uploading={uploading}
                sending={sending}
                onBack={() => navigate('/eventos')}
                onGenerate={handleGenerate}
                onManualUpload={handleManualUpload}
                onOpenSendModal={openSendModal}
                onResetGeneratedContract={handleResetGeneratedContract}
              />
            </div>
          </div>
        )}
      </div>
      {sendOpen && (
        <div className="modal-overlay">
          <div className="event-edit-modal signature-modal">
            <div className="pdf-modal-header">
              <div>
                <strong>Enviar para assinatura</strong>
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
              <button type="button" className="btn btn-primary" onClick={handleSendAndNavigate} disabled={sending}>
                <Send size={15} /> {sending ? 'Enviando...' : 'Enviar para assinatura'}
              </button>
            </div>
          </div>
        </div>
      )}
      {toast && <div className={`toast ${toast.type}`}>{toast.msg}</div>}
    </>
  )
}
