import { useEffect, useMemo, useState } from 'react'
import { eventsService } from '../../events/services/eventsService'
import { templatesService } from '../../templates/services/templatesService'
import { contractsService } from '../services/contractsService'
import { isTemplateCompatible } from '../utils/generateContractUtils'

export function useContractGenerationData({ generatedContract, initialEventId = '', showToast }) {
  const [eventos, setEventos] = useState([])
  const [contracts, setContracts] = useState([])
  const [templates, setTemplates] = useState([])
  const [selectedEventId, setSelectedEventId] = useState(initialEventId)
  const [selectedTemplateId, setSelectedTemplateId] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      eventsService.list(),
      templatesService.list(),
      contractsService.list(),
    ])
      .then(([eventsData, templatesData, contractsData]) => {
        setEventos(Array.isArray(eventsData) ? eventsData : [])
        const nextTemplates = Array.isArray(templatesData) ? templatesData : []
        setTemplates(nextTemplates)
        setContracts(Array.isArray(contractsData) ? contractsData : [])
        if (nextTemplates?.[0]?.id) {
          setSelectedTemplateId(String(nextTemplates[0].id))
        }
      })
      .catch(() => showToast('Não foi possível carregar eventos e templates.', 'error'))
      .finally(() => setLoading(false))
  }, [showToast])

  const selectedEvent = useMemo(
    () => eventos.find(evento => String(evento.id) === String(selectedEventId)),
    [eventos, selectedEventId]
  )

  const availableEventos = useMemo(() => {
    const eventIdsWithContract = new Set(contracts.map(contract => String(contract.event?.id)).filter(Boolean))
    return eventos.filter(evento => {
      if (String(evento.id) === String(selectedEventId) && generatedContract) return true
      return !eventIdsWithContract.has(String(evento.id))
    })
  }, [contracts, eventos, generatedContract, selectedEventId])

  const selectedTemplate = useMemo(
    () => templates.find(template => String(template.id) === String(selectedTemplateId)),
    [templates, selectedTemplateId]
  )

  const visibleTemplates = useMemo(() => {
    if (!selectedEvent) return templates
    const compatible = templates.filter(template => isTemplateCompatible(selectedEvent, template))
    return compatible.length > 0 ? compatible : templates
  }, [selectedEvent, templates])

  useEffect(() => {
    if (!visibleTemplates.length) {
      setSelectedTemplateId('')
      return
    }

    const stillVisible = visibleTemplates.some(template => String(template.id) === String(selectedTemplateId))
    if (!stillVisible) {
      setSelectedTemplateId(String(visibleTemplates[0].id))
    }
  }, [visibleTemplates, selectedTemplateId])

  const upsertContract = contract => {
    setContracts(current => {
      const exists = current.some(item => item.id === contract.id)
      return exists ? current.map(item => item.id === contract.id ? contract : item) : [...current, contract]
    })
  }

  return {
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
  }
}
