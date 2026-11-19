import { useEffect, useMemo, useState } from 'react'
import { formatDateBR, parseLocalDate } from '../../../utils/date'
import { nextSort, sortItems } from '../../../utils/sort'
import { contractsService } from '../../contracts/services/contractsService'
import { eventsService } from '../services/eventsService'

const PAGE_SIZE = 8

const MOCK_EVENTOS = [
  { id: 1, client: { nome: 'TechSolutions Inc.' }, tipoEvento: 'Corporativo', data: '2023-10-12', valor: 15400, status: 'ASSINADO', ref: 'REV-8821' },
  { id: 2, client: { nome: 'Global Partners LLC' }, tipoEvento: 'Conferência', data: '2023-10-15', valor: 42000, status: 'ENVIADO', ref: 'REV-8822' },
  { id: 3, client: { nome: 'Ana Rodrigues Wedding' }, tipoEvento: 'Social', data: '2023-10-21', valor: 8900, status: 'ASSINADO', ref: 'REV-8825' },
  { id: 4, client: { nome: 'MarketFlow Logistics' }, tipoEvento: 'Corporativo', data: '2023-10-25', valor: 22150, status: '', ref: 'REV-8829' },
]

export const EVENT_STATUS_MAP = {
  ASSINADO: { label: 'Assinado', cls: 'status-assinado' },
  ENVIADO: { label: 'Pendente de assinatura', cls: 'status-pendente' },
  RASCUNHO: { label: 'Gerado', cls: 'status-rascunho' },
  DISPENSADO: { label: 'Sem contrato necessário', cls: 'status-sem-contrato' },
  '': { label: 'Sem contrato', cls: 'status-sem-contrato' },
}

function isUpcomingDays(value, days) {
  if (!value) return false

  const eventDate = parseLocalDate(value)
  const today = parseLocalDate(new Date())
  if (!eventDate || !today) return false

  const diffDays = Math.round((eventDate.getTime() - today.getTime()) / 86400000)
  return diffDays >= 0 && diffDays <= days
}

export function useEventsPage() {
  const [eventos, setEventos] = useState(MOCK_EVENTOS)
  const [filter, setFilter] = useState('todos')
  const [searchTerm, setSearchTerm] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const [sortConfig, setSortConfig] = useState({ key: 'data', direction: 'asc' })
  const [sendingId, setSendingId] = useState(null)
  const [sendTarget, setSendTarget] = useState(null)
  const [additionalSigners, setAdditionalSigners] = useState([])
  const [toast, setToast] = useState(null)

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3500)
  }

  useEffect(() => {
    Promise.all([
      eventsService.list(),
      contractsService.list(),
    ])
      .then(([eventsData, contractsData]) => {
        const contractsByEvent = new Map((contractsData || []).map(contract => [contract.event?.id, contract]))
        setEventos((eventsData || []).map(evento => {
          const contract = contractsByEvent.get(evento.id)
          return {
            ...evento,
            contract,
            status: contract?.status || (evento.contratoDispensado ? 'DISPENSADO' : ''),
          }
        }))
      })
      .catch(() => {})
  }, [])

  useEffect(() => {
    setCurrentPage(1)
  }, [filter, searchTerm])

  const handleSendContract = async (event, contract) => {
    setSendingId(contract.id)
    try {
      const signers = additionalSigners
        .map(signer => ({
          nome: String(signer.nome || '').trim(),
          email: String(signer.email || '').trim(),
        }))
        .filter(signer => signer.email)

      const sentContract = await contractsService.send(contract.id, signers)
      setEventos(current => current.map(item => {
        if (item.id !== event.id) return item
        return {
          ...item,
          contract: sentContract,
          status: sentContract.status || item.status,
        }
      }))
      showToast('Contrato enviado para assinatura!', 'success')
      setSendTarget(null)
      setAdditionalSigners([])
    } catch (err) {
      showToast(err.message || 'Erro ao enviar contrato.', 'error')
    } finally {
      setSendingId(null)
    }
  }

  const openSendModal = (event, contract) => {
    setSendTarget({ event, contract })
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

  const filtered = useMemo(() => eventos.filter(event => {
    const term = searchTerm.trim().toLowerCase()
    const matchesSearch = !term || [
      event.client?.nome,
      event.tipoEvento,
      event.local,
      event.ref,
      event.status,
      formatDateBR(event.data),
    ].some(value => String(value || '').toLowerCase().includes(term))

    if (!matchesSearch) return false
    if (filter === 'sem-contrato') return !event.status
    if (filter === 'dispensados') return event.status === 'DISPENSADO'
    if (filter === 'gerados') return event.status === 'RASCUNHO'
    if (filter === 'assinatura') return event.status === 'ENVIADO'
    if (filter === 'assinados') return event.status === 'ASSINADO'
    if (filter === 'proximos') return isUpcomingDays(event.data, 7)
    if (filter === 'proximos15') return isUpcomingDays(event.data, 15)
    return true
  }), [eventos, filter, searchTerm])

  const sorted = useMemo(() => sortItems(filtered, sortConfig, {
    cliente: { value: item => item.client?.nome, type: 'text' },
    tipo: { value: item => item.tipoEvento, type: 'text' },
    data: { value: item => item.data, type: 'date' },
    valor: { value: item => item.valor, type: 'number' },
    contrato: { value: item => EVENT_STATUS_MAP[(item.status || '').toUpperCase()]?.label || 'Sem contrato', type: 'text' },
  }), [filtered, sortConfig])

  const totalPages = Math.max(Math.ceil(sorted.length / PAGE_SIZE), 1)
  const safePage = Math.min(currentPage, totalPages)
  const pageStart = (safePage - 1) * PAGE_SIZE
  const paginated = sorted.slice(pageStart, pageStart + PAGE_SIZE)

  const handleSort = key => {
    setSortConfig(current => nextSort(current, key))
  }

  const previousPage = () => {
    setCurrentPage(page => Math.max(page - 1, 1))
  }

  const nextPage = () => {
    setCurrentPage(page => Math.min(page + 1, totalPages))
  }

  return {
    additionalSigners,
    addAdditionalSigner,
    filter,
    filtered,
    handleSendContract,
    handleSort,
    nextPage,
    openSendModal,
    pageStart,
    paginated,
    previousPage,
    removeAdditionalSigner,
    safePage,
    searchTerm,
    sendTarget,
    sendingId,
    setCurrentPage,
    setFilter,
    setSearchTerm,
    setSendTarget,
    sortConfig,
    toast,
    totalPages,
    updateAdditionalSigner,
  }
}
