import { useState } from 'react'
import { contractsService } from '../services/contractsService'

export function useContractGenerationActions({
  generatedContract,
  normalizedSigners,
  selectedEventId,
  selectedTemplateId,
  showToast,
  closeSendModal,
  resetAdditionalSigners,
  resetManualUpload,
  setGeneratedContract,
  setSelectedEventId,
  upsertContract,
}) {
  const [saving, setSaving] = useState(false)
  const [sending, setSending] = useState(false)

  const handleGenerate = async () => {
    if (!selectedEventId || !selectedTemplateId) {
      showToast('Selecione um evento e um template.', 'error')
      return null
    }

    setSaving(true)
    try {
      const contract = await contractsService.generate({
        eventId: Number(selectedEventId),
        templateId: Number(selectedTemplateId),
      })
      setGeneratedContract(contract)
      upsertContract(contract)
      showToast('Contrato gerado com sucesso!', 'success')
      return contract
    } catch (err) {
      showToast(err.message || 'Erro ao gerar contrato.', 'error')
      return null
    } finally {
      setSaving(false)
    }
  }

  const handleSend = async () => {
    if (!generatedContract?.id) {
      showToast('Gere o contrato antes de enviar para assinatura.', 'error')
      return null
    }

    setSending(true)
    try {
      const contract = await contractsService.send(generatedContract.id, normalizedSigners)
      setGeneratedContract(contract)
      showToast('Contrato enviado para assinatura!', 'success')
      closeSendModal()
      resetAdditionalSigners()
      return contract
    } catch (err) {
      showToast(err.message || 'Erro ao enviar contrato.', 'error')
      return null
    } finally {
      setSending(false)
    }
  }

  const handleResetGeneratedContract = () => {
    setGeneratedContract(null)
    setSelectedEventId('')
    resetManualUpload()
  }

  return {
    handleGenerate,
    handleResetGeneratedContract,
    handleSend,
    saving,
    sending,
  }
}
