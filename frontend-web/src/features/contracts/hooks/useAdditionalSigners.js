import { useState } from 'react'

export function useAdditionalSigners() {
  const [sendOpen, setSendOpen] = useState(false)
  const [additionalSigners, setAdditionalSigners] = useState([])

  const openSendModal = () => {
    setAdditionalSigners([])
    setSendOpen(true)
  }

  const closeSendModal = () => {
    setSendOpen(false)
  }

  const resetAdditionalSigners = () => {
    setAdditionalSigners([])
  }

  const addAdditionalSigner = () => {
    setAdditionalSigners(current => [...current, { nome: '', email: '' }])
  }

  const updateAdditionalSigner = (index, field, value) => {
    setAdditionalSigners(current => current.map((signer, currentIndex) => (
      currentIndex === index ? { ...signer, [field]: value } : signer
    )))
  }

  const removeAdditionalSigner = index => {
    setAdditionalSigners(current => current.filter((_, currentIndex) => currentIndex !== index))
  }

  const normalizedSigners = additionalSigners
    .map(signer => ({
      nome: String(signer.nome || '').trim(),
      email: String(signer.email || '').trim(),
    }))
    .filter(signer => signer.email)

  return {
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
  }
}
