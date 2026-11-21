import { useEffect, useEffectEvent, useRef, useState } from 'react'
import { useToast } from '../../../shared/hooks/useToast'
import { clientsService } from '../../clients/services/clientsService'
import { contractsService } from '../../contracts/services/contractsService'
import { eventsService } from '../services/eventsService'
import { useAdditionalSigners } from '../../contracts/hooks/useAdditionalSigners'

export function useEventDetailPage({
  id,
  shouldOpenEdit,
  onEditParamHandled,
  helpers,
}) {
  const [evento, setEvento] = useState(null)
  const [contrato, setContrato] = useState(null)
  const [clientes, setClientes] = useState([])
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [savingEvent, setSavingEvent] = useState(false)
  const [savingPayment, setSavingPayment] = useState(false)
  const [reviewing, setReviewing] = useState(false)
  const [pdfOpen, setPdfOpen] = useState(false)
  const [editOpen, setEditOpen] = useState(false)
  const [eventForm, setEventForm] = useState(null)
  const [confirmDialog, setConfirmDialog] = useState(null)
  const handledAutoEditEventIdRef = useRef(null)
  const { showToast, toast } = useToast()
  const {
    additionalSigners,
    normalizedSigners,
    sendOpen,
    addAdditionalSigner,
    openSendModal,
    removeAdditionalSigner,
    resetAdditionalSigners,
    setSendOpen,
    updateAdditionalSigner,
  } = useAdditionalSigners()

  const currentContractStatus = contrato?.status || (evento?.contratoDispensado ? 'DISPENSADO' : 'SEM_CONTRATO')
  const sendActionLabel = currentContractStatus === 'RASCUNHO'
    ? 'Enviar para assinatura'
    : 'Reenviar para assinatura'
  const canSendContract = currentContractStatus === 'RASCUNHO' || currentContractStatus === 'ENVIADO'
  const contractualFieldsLocked = currentContractStatus === 'ENVIADO' || currentContractStatus === 'ASSINADO'
  const canReviewContract = currentContractStatus === 'ENVIADO'

  useEffect(() => {
    let ignore = false

    async function loadEvent() {
      try {
        const evtData = await eventsService.get(id)
        const contractData = await contractsService.getByEventOrNull(evtData.id)

        if (!ignore) {
          setEvento(evtData)
          setContrato(contractData)
        }
      } catch (err) {
        console.error(err)
        if (!ignore) showToast('Erro ao carregar os dados do evento', 'error')
      } finally {
        if (!ignore) setLoading(false)
      }
    }

    loadEvent()

    return () => {
      ignore = true
    }
  }, [id, showToast])

  const buildEventForm = event => {
    const dadosServico = helpers.parseDadosServico(event.dadosServico)

    return {
      clienteId: event.client?.id ? String(event.client.id) : '',
      tipoServico: event.tipoServico || 'PINTURA_AO_VIVO',
      tipoEvento: event.tipoEvento || 'Corporativo',
      data: event.data || '',
      horarioInicio: helpers.formatTime(dadosServico.horarioInicio || event.horarioInicio),
      duracaoEstimada: dadosServico.duracaoEstimada || event.duracaoEstimada || '',
      local: event.local || '',
      numeroConvidados: dadosServico.numeroConvidados ?? event.numeroConvidados ?? '',
      tecnica: dadosServico.tecnica || event.tecnica || '',
      tamanhoTela: dadosServico.tamanhoTela || event.tamanhoTela || '',
      cenaRetratada: dadosServico.cenaRetratada || event.cenaRetratada || '',
      entregaObra: dadosServico.entregaObra || event.entregaObra || '',
      valor: helpers.formatCurrencyInput(event.valor),
      valorSinal: helpers.formatCurrencyInput(event.valorSinal),
      formaPagamentoSinal: event.formaPagamentoSinal || '',
      formaPagamentoRestante: event.formaPagamentoRestante || '',
      tipoProduto: dadosServico.tipoProduto || helpers.produtosEncomenda[0],
      dimensao: dadosServico.dimensao || '',
      temaReferencia: dadosServico.temaReferencia || '',
      prazoEstimado: dadosServico.prazoEstimado || '',
      formaEntrega: dadosServico.formaEntrega || 'ENVIO',
      enderecoEnvio: dadosServico.enderecoEnvio || '',
      freteResponsavel: dadosServico.freteResponsavel || '',
      prazoDiasUteis: dadosServico.prazoDiasUteis || '',
      rodadasAjustes: dadosServico.rodadasAjustes || '',
      observacoes: event.observacoes || '',
      contratoDispensado: event.contratoDispensado || false,
    }
  }

  const openEditModal = () => {
    setEventForm(buildEventForm(evento))
    setEditOpen(true)

    if (clientes.length === 0) {
      clientsService.list()
        .then(data => setClientes(Array.isArray(data) ? data : []))
        .catch(() => setClientes([]))
    }
  }

  // useEffectEvent: o efeito usa a versão mais recente de openEditModal sem rodar de novo quando ela muda
  const openEditModalFromEffect = useEffectEvent(() => openEditModal())

  useEffect(() => {
    if (!evento || !shouldOpenEdit) return

    const eventId = String(evento.id || '')
    if (handledAutoEditEventIdRef.current === eventId) return

    handledAutoEditEventIdRef.current = eventId
    openEditModalFromEffect()
    onEditParamHandled(evento)
  }, [evento, shouldOpenEdit, onEditParamHandled])

  const hasContractualChanges = form => {
    if (!evento || !form) return false

    return String(evento.client?.id || '') !== String(form.clienteId || '')
      || String(evento.tipoServico || '') !== String(form.tipoServico || '')
      || helpers.normalizeDadosServico(evento.dadosServico) !== helpers.normalizeDadosServico(helpers.buildDadosServico(form))
      || String(evento.tipoEvento || '') !== String(form.tipoEvento || '')
      || String(evento.data || '') !== String(form.data || '')
      || String(evento.horarioInicio || '') !== String(form.horarioInicio || '')
      || String(evento.duracaoEstimada || '') !== String(form.duracaoEstimada || '')
      || String(evento.local || '') !== String(form.local || '')
      || String(evento.numeroConvidados || '') !== String(form.numeroConvidados || '')
      || String(evento.tecnica || '') !== String(form.tecnica || '')
      || String(evento.tamanhoTela || '') !== String(form.tamanhoTela || '')
      || String(evento.cenaRetratada || '') !== String(form.cenaRetratada || '')
      || String(evento.entregaObra || '') !== String(form.entregaObra || '')
      || Number(evento.valor || 0) !== helpers.parseCurrency(form.valor)
      || Number(evento.valorSinal || 0) !== helpers.parseCurrency(form.valorSinal)
      || String(evento.formaPagamentoSinal || '') !== String(form.formaPagamentoSinal || '')
      || String(evento.formaPagamentoRestante || '') !== String(form.formaPagamentoRestante || '')
  }

  const refreshContract = async eventId => {
    try {
      setContrato(await contractsService.getByEventOrNull(eventId))
    } catch (err) {
      const message = String(err?.message || '').toLowerCase()
      const contractWasInvalidated = err?.status === 404
        || message.includes('contrato nao encontrado para o evento')
        || message.includes('contrato não encontrado para o evento')

      if (contractWasInvalidated) {
        setContrato(null)
        return
      }

      throw err
    }
  }

  const setEventField = (field, value) => {
    setEventForm(current => ({
      ...current,
      [field]: value,
      ...(field === 'formaEntrega' && value === 'RETIRADA' ? { freteResponsavel: '' } : {}),
    }))
  }

  const saveEvent = async () => {
    if (!eventForm) return

    setSavingEvent(true)
    try {
      const showLiveFields = helpers.isLiveService(eventForm.tipoServico)
      const updatedEvent = await eventsService.update(evento.id, {
        clienteId: Number(eventForm.clienteId),
        tipoServico: eventForm.tipoServico,
        dadosServico: JSON.stringify(helpers.buildDadosServico(eventForm)),
        tipoEvento: showLiveFields ? eventForm.tipoEvento : helpers.fallbackTipoEvento(eventForm.tipoServico),
        data: showLiveFields ? eventForm.data : (eventForm.data || helpers.todayISO()),
        horarioInicio: showLiveFields ? eventForm.horarioInicio || null : null,
        duracaoEstimada: showLiveFields ? eventForm.duracaoEstimada : null,
        local: showLiveFields ? eventForm.local : 'Não se aplica',
        numeroConvidados: showLiveFields && eventForm.numeroConvidados ? Number(eventForm.numeroConvidados) : null,
        tecnica: showLiveFields ? eventForm.tecnica : null,
        tamanhoTela: showLiveFields ? eventForm.tamanhoTela : null,
        cenaRetratada: showLiveFields ? eventForm.cenaRetratada : null,
        entregaObra: showLiveFields ? eventForm.entregaObra : null,
        valor: helpers.parseCurrency(eventForm.valor),
        valorSinal: helpers.parseCurrency(eventForm.valorSinal),
        formaPagamentoSinal: eventForm.formaPagamentoSinal,
        formaPagamentoRestante: eventForm.formaPagamentoRestante,
        observacoes: helpers.nullableText(eventForm.observacoes),
        contratoDispensado: eventForm.contratoDispensado,
      })
      setEvento(updatedEvent)
      setEditOpen(false)
      await refreshContract(updatedEvent.id)
      showToast('Evento atualizado com sucesso!', 'success')
    } catch (err) {
      showToast(err.message || 'Erro ao salvar evento.', 'error')
    } finally {
      setSavingEvent(false)
      setConfirmDialog(null)
    }
  }

  const handleSaveEvent = async event => {
    event.preventDefault()
    if (!eventForm) return

    if (currentContractStatus === 'RASCUNHO' && hasContractualChanges(eventForm)) {
      setConfirmDialog({
        title: 'Invalidar contrato gerado?',
        description: 'Alterar dados contratuais vai apagar o PDF gerado. Depois será necessário gerar um novo contrato.',
        confirmLabel: 'Invalidar e salvar',
        tone: 'warning',
        onConfirm: saveEvent,
      })
      return
    }

    await saveEvent()
  }

  const reviewContract = async () => {
    setReviewing(true)
    try {
      const reviewedContract = await contractsService.requestReview(contrato.id)
      setContrato(reviewedContract)
      if (reviewedContract.status === 'ASSINADO') {
        showToast('Contrato já estava assinado no Autentique. Status atualizado para Assinado.', 'success')
      } else {
        showToast('Contrato voltou para Gerado. Agora você pode revisar o evento.', 'success')
      }
    } catch (err) {
      showToast(err.message || 'Erro ao revisar contrato.', 'error')
    } finally {
      setReviewing(false)
      setConfirmDialog(null)
    }
  }

  const handleReviewContract = async () => {
    if (!contrato?.id) return

    setConfirmDialog({
      title: 'Revisar contrato?',
      description: 'Antes de revisar, o sistema vai consultar o Autentique. Se o contrato já estiver assinado, ele será marcado como Assinado e não poderá voltar para Gerado.',
      confirmLabel: 'Revisar contrato',
      tone: 'warning',
      onConfirm: reviewContract,
    })
  }

  const handleSend = async () => {
    if (!contrato?.id) {
      showToast('Gere o contrato antes de enviar para assinatura.', 'error')
      return
    }

    setSending(true)
    try {
      const sentContract = await contractsService.send(contrato.id, normalizedSigners)
      setContrato(sentContract)
      if (sentContract.status === 'ASSINADO') {
        showToast('Contrato já estava assinado no Autentique. Status atualizado para Assinado.', 'success')
      } else {
        showToast('Contrato enviado para o Autentique!', 'success')
      }
      setSendOpen(false)
      resetAdditionalSigners()
    } catch (err) {
      showToast(err.message || 'Erro ao enviar. Servidor offline?', 'error')
    } finally {
      setSending(false)
    }
  }

  const savePayment = async (paymentForm) => {
    if (!evento) return
    setSavingPayment(true)
    try {
      const updatedEvent = await eventsService.updatePayment(evento.id, paymentForm)
      setEvento(updatedEvent)
      showToast('Pagamento atualizado com sucesso!', 'success')
    } catch (err) {
      showToast(err.message || 'Erro ao salvar pagamento.', 'error')
    } finally {
      setSavingPayment(false)
    }
  }

  const handleViewPdf = () => {
    if (!contrato?.hasPdf) {
      showToast('PDF ainda não foi gerado para este contrato.', 'error')
      return
    }

    setPdfOpen(true)
  }

  return {
    additionalSigners,
    addAdditionalSigner,
    canReviewContract,
    canSendContract,
    clientes,
    confirmDialog,
    contrato,
    contractualFieldsLocked,
    currentContractStatus,
    editOpen,
    eventForm,
    evento,
    handleReviewContract,
    handleSaveEvent,
    handleSend,
    handleViewPdf,
    loading,
    openEditModal,
    openSendModal,
    pdfOpen,
    removeAdditionalSigner,
    reviewing,
    savePayment,
    savingEvent,
    savingPayment,
    sendActionLabel,
    sending,
    sendOpen,
    setConfirmDialog,
    setEditOpen,
    setEventField,
    setPdfOpen,
    setSendOpen,
    toast,
    updateAdditionalSigner,
  }
}
