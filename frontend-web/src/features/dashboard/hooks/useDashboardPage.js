import { useEffect, useMemo, useState } from 'react'
import { contractsService } from '../../contracts/services/contractsService'
import { eventsService } from '../../events/services/eventsService'
import { formatDateBR, parseLocalDate } from '../../../utils/date'
import { nextSort, sortItems } from '../../../utils/sort'

const MOCK_EVENTS = [
  { id: 1, client: { nome: 'Alpha Solutions' }, tipoEvento: 'Conferência', data: '2024-05-12', valor: 45000, status: 'ASSINADO' },
  { id: 2, client: { nome: 'Blue Tech Group' }, tipoEvento: 'Workshop estratégico', data: '2024-05-15', valor: 12500, status: 'ENVIADO' },
  { id: 3, client: { nome: 'Lumina Media' }, tipoEvento: 'Lançamento de produto', data: '2024-05-18', valor: 28900, status: 'ENVIADO' },
  { id: 4, client: { nome: 'Vortex Global' }, tipoEvento: 'Jantar executivo', data: '2024-05-22', valor: 8200, status: 'ASSINADO' },
]

function isCurrentMonth(value) {
  if (!value) return false
  const date = parseLocalDate(value)
  const now = new Date()
  return date ? date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear() : false
}

function normalizeDate(value) {
  return parseLocalDate(value)
}

function statusLabel(status) {
  const map = {
    ASSINADO: 'Assinado',
    ENVIADO: 'Pendente de assinatura',
    RASCUNHO: 'Gerado',
    '': 'Sem contrato',
  }
  return map[status] || status
}

export function useDashboardPage() {
  const [eventos, setEventos] = useState(MOCK_EVENTS)
  const [searchTerm, setSearchTerm] = useState('')
  const [sortConfig, setSortConfig] = useState({ key: 'data', direction: 'asc' })

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

  const metrics = useMemo(() => {
    const totalReceita = eventos.reduce((sum, event) => sum + (event.valor || 0), 0)
    return {
      eventosMes: eventos.filter(item => isCurrentMonth(item.data)).length,
      pendentesEnvio: eventos.filter(event => (event.status || '').toUpperCase() === 'RASCUNHO').length,
      pendentes: eventos.filter(event => (event.status || '').toUpperCase() === 'ENVIADO').length,
      receitaMedia: eventos.length > 0 ? totalReceita / eventos.length : 0,
      totalReceita,
    }
  }, [eventos])

  const upcomingEventos = useMemo(() => {
    return eventos
      .filter(event => {
        const date = normalizeDate(event.data)
        const today = normalizeDate(new Date())
        return date && today && date >= today
      })
      .sort((a, b) => normalizeDate(a.data) - normalizeDate(b.data))
  }, [eventos])

  const visibleEventos = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    if (!term) return upcomingEventos

    return upcomingEventos.filter(event => (
      [
        event.client?.nome,
        event.tipoEvento,
        event.local,
        event.status,
        formatDateBR(event.data),
      ].some(value => String(value || '').toLowerCase().includes(term))
    ))
  }, [searchTerm, upcomingEventos])

  const sortedVisibleEventos = useMemo(() => sortItems(visibleEventos, sortConfig, {
    cliente: { value: item => item.client?.nome, type: 'text' },
    tipo: { value: item => item.tipoEvento, type: 'text' },
    data: { value: item => item.data, type: 'date' },
    valor: { value: item => item.valor, type: 'number' },
    status: { value: item => statusLabel((item.status || '').toUpperCase()), type: 'text' },
  }), [sortConfig, visibleEventos])

  const handleSort = key => {
    setSortConfig(current => nextSort(current, key))
  }

  return {
    ...metrics,
    searchTerm,
    setSearchTerm,
    sortConfig,
    sortedVisibleEventos,
    visibleEventos,
    handleSort,
  }
}
