import { useEffect, useMemo, useState } from 'react'
import { nextSort, sortItems } from '../../../utils/sort'
import { contractsService } from '../services/contractsService'

export const CONTRACT_STATUS_MAP = {
  ASSINADO: { label: 'Assinado', cls: 'status-assinado' },
  ENVIADO: { label: 'Pendente de assinatura', cls: 'status-pendente' },
  RASCUNHO: { label: 'Gerado', cls: 'status-rascunho' },
}

export function useContractsPage() {
  const [contratos, setContratos] = useState([])
  const [loading, setLoading] = useState(true)
  const [sortConfig, setSortConfig] = useState({ key: 'cliente', direction: 'asc' })
  const [sendingId, setSendingId] = useState(null)
  const [toast, setToast] = useState(null)

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3500)
  }

  const fetchContratos = () => {
    setLoading(true)
    contractsService.list()
      .then(data => setContratos(Array.isArray(data) ? data : []))
      .catch(() => setContratos([]))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    fetchContratos()
  }, [])

  const handleSend = async contractId => {
    setSendingId(contractId)
    try {
      await contractsService.send(contractId)
      showToast('Contrato enviado para assinatura!', 'success')
      fetchContratos()
    } catch (err) {
      showToast(err.message || 'Erro ao enviar contrato.', 'error')
    } finally {
      setSendingId(null)
    }
  }

  const sortedContratos = useMemo(() => sortItems(contratos, sortConfig, {
    cliente: { value: item => item.event?.client?.nome, type: 'text' },
    evento: { value: item => item.event?.tipoEvento, type: 'text' },
    template: { value: item => item.template?.nome, type: 'text' },
    status: { value: item => CONTRACT_STATUS_MAP[item.status]?.label || item.status, type: 'text' },
    pdf: { value: item => item.pdfUrl ? 'Gerado' : 'Pendente', type: 'text' },
  }), [contratos, sortConfig])

  const handleSort = key => {
    setSortConfig(current => nextSort(current, key))
  }

  return {
    contratos,
    handleSend,
    handleSort,
    loading,
    sendingId,
    sortConfig,
    sortedContratos,
    toast,
  }
}
