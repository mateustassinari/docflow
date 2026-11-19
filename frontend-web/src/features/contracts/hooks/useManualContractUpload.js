import { useState } from 'react'
import { contractsService } from '../services/contractsService'

export function useManualContractUpload({ selectedEventId, selectedTemplateId, showToast, upsertContract, setGeneratedContract }) {
  const [manualFile, setManualFile] = useState(null)
  const [manualUploadSigned, setManualUploadSigned] = useState(false)
  const [uploading, setUploading] = useState(false)

  const resetManualUpload = () => {
    setManualFile(null)
    setManualUploadSigned(false)
  }

  const handleManualUpload = async () => {
    if (!selectedEventId || !selectedTemplateId) {
      showToast('Selecione um evento e um template antes do upload.', 'error')
      return null
    }

    if (!manualFile) {
      showToast('Selecione um arquivo PDF para upload.', 'error')
      return null
    }

    const fileName = manualFile.name.toLowerCase()
    if (manualFile.type !== 'application/pdf' && !fileName.endsWith('.pdf')) {
      showToast('O arquivo precisa estar em formato PDF.', 'error')
      return null
    }

    const formData = new FormData()
    formData.append('eventId', selectedEventId)
    formData.append('templateId', selectedTemplateId)
    formData.append('signed', String(manualUploadSigned))
    formData.append('file', manualFile)

    setUploading(true)
    try {
      const contract = await contractsService.uploadManual(formData)
      setGeneratedContract(contract)
      upsertContract(contract)
      resetManualUpload()
      showToast(manualUploadSigned ? 'PDF assinado vinculado ao evento com sucesso!' : 'PDF manual vinculado ao evento com sucesso!', 'success')
      return contract
    } catch (err) {
      showToast(err.message || 'Erro ao enviar PDF manual.', 'error')
      return null
    } finally {
      setUploading(false)
    }
  }

  return {
    manualFile,
    manualUploadSigned,
    uploading,
    handleManualUpload,
    resetManualUpload,
    setManualFile,
    setManualUploadSigned,
  }
}
