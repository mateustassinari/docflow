import { ExternalLink, Pencil } from 'lucide-react'
import { contractsService } from '../services/contractsService'

export default function ContractPdfPreview({
  contract,
  onReviewData,
}) {
  if (!contract?.hasPdf) return null

  return (
    <div className="contract-pdf-preview">
      <div className="contract-pdf-preview-header">
        <div>
          <strong>Preview do PDF</strong>
          <span>{contract.status === 'ASSINADO' ? 'Via assinada salva no sistema.' : 'Confira o contrato antes de enviar para assinatura.'}</span>
        </div>
        <div className="contract-pdf-preview-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={onReviewData}
          >
            <Pencil size={14} />
            Revisar dados
          </button>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => window.open(contractsService.pdfUrl(contract.id), '_blank')}
          >
            <ExternalLink size={14} />
            Abrir maior
          </button>
        </div>
      </div>
      <iframe
        title="Preview do contrato gerado"
        className="contract-pdf-preview-frame"
        src={`${contractsService.pdfUrl(contract.id)}#toolbar=1&navpanes=0&view=FitH`}
      />
    </div>
  )
}
