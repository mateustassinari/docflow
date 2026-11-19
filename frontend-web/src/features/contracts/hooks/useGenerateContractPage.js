import { useState } from 'react'
import { useToast } from '../../../shared/hooks/useToast'
import { useAdditionalSigners } from './useAdditionalSigners'
import { useContractGenerationActions } from './useContractGenerationActions'
import { useContractGenerationData } from './useContractGenerationData'
import { useManualContractUpload } from './useManualContractUpload'

export function useGenerateContractPage(initialEventId = '') {
  const [generatedContract, setGeneratedContract] = useState(null)
  const { showToast, toast } = useToast()

  const {
    additionalSigners,
    normalizedSigners,
    sendOpen,
    addAdditionalSigner,
    closeSendModal,
    openSendModal,
    removeAdditionalSigner,
    resetAdditionalSigners,
    setSendOpen,
    updateAdditionalSigner,
  } = useAdditionalSigners()

  const {
    availableEventos,
    loading,
    selectedEvent,
    selectedEventId,
    selectedTemplate,
    selectedTemplateId,
    templates,
    visibleTemplates,
    setSelectedEventId,
    setSelectedTemplateId,
    upsertContract,
  } = useContractGenerationData({
    generatedContract,
    initialEventId,
    showToast,
  })

  const {
    manualFile,
    manualUploadSigned,
    uploading,
    handleManualUpload,
    resetManualUpload,
    setManualFile,
    setManualUploadSigned,
  } = useManualContractUpload({
    selectedEventId,
    selectedTemplateId,
    showToast,
    upsertContract,
    setGeneratedContract,
  })

  const {
    handleGenerate,
    handleResetGeneratedContract,
    handleSend,
    saving,
    sending,
  } = useContractGenerationActions({
    closeSendModal,
    generatedContract,
    normalizedSigners,
    resetAdditionalSigners,
    resetManualUpload,
    selectedEventId,
    selectedTemplateId,
    showToast,
    setGeneratedContract,
    setSelectedEventId,
    upsertContract,
  })

  const selectEvent = eventId => {
    setSelectedEventId(eventId)
    setGeneratedContract(null)
    resetManualUpload()
  }

  const selectTemplate = templateId => {
    setSelectedTemplateId(templateId)
    setGeneratedContract(null)
    resetManualUpload()
  }

  return {
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
  }
}
