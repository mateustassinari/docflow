import { useEffect, useState } from 'react'
import { useToast } from '../../../shared/hooks/useToast'
import { maskCpfCnpj, maskPhone, parseCurrency } from '../../../utils/masks'
import { clientsService } from '../../clients/services/clientsService'
import { eventsService } from '../services/eventsService'

const EMPTY_EVENT_FORM = {
  clienteNome: '',
  clienteEmail: '',
  clienteTelefone: '',
  clienteCpf: '',
  tipoServico: 'PINTURA_AO_VIVO',
  tipoEvento: 'Corporativo',
  data: '',
  horarioInicio: '',
  duracaoEstimada: '',
  local: '',
  numeroConvidados: '',
  tecnica: '',
  tamanhoTela: '',
  cenaRetratada: '',
  entregaObra: '',
  valor: '',
  valorSinal: '',
  formaPagamentoSinal: '',
  formaPagamentoRestante: '',
  tipoProduto: 'Pintura artística',
  dimensao: '',
  temaReferencia: '',
  prazoEstimado: '',
  formaEntrega: 'ENVIO',
  enderecoEnvio: '',
  freteResponsavel: '',
  prazoDiasUteis: '',
  rodadasAjustes: '',
  observacoes: '',
  contratoDispensado: false,
}

function todayISO() {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function usesEventoAoVivoFields(tipoServico) {
  return tipoServico === 'PINTURA_AO_VIVO' || tipoServico === 'AQUARELA_AO_VIVO'
}

function getFallbackTipoEvento(tipoServico) {
  if (tipoServico === 'ENCOMENDA_ARTISTICA') return 'Encomenda artística'
  if (tipoServico === 'IDENTIDADE_VISUAL') return 'Identidade visual'
  return ''
}

function nullableText(value) {
  const trimmed = String(value || '').trim()
  return trimmed ? trimmed : null
}

function buildDadosServico(form) {
  if (form.tipoServico === 'ENCOMENDA_ARTISTICA') {
    return {
      tipoProduto: form.tipoProduto,
      dimensao: form.dimensao,
      tecnica: form.tecnica,
      temaReferencia: form.temaReferencia,
      prazoEstimado: form.prazoEstimado,
      formaEntrega: form.formaEntrega,
      enderecoEnvio: form.enderecoEnvio,
      freteResponsavel: form.formaEntrega === 'ENVIO' ? form.freteResponsavel : '',
    }
  }

  if (form.tipoServico === 'IDENTIDADE_VISUAL') {
    return {
      prazoDiasUteis: form.prazoDiasUteis,
      rodadasAjustes: form.rodadasAjustes,
    }
  }

  return {
    horarioInicio: form.horarioInicio,
    duracaoEstimada: form.duracaoEstimada,
    numeroConvidados: form.numeroConvidados,
    tecnica: form.tecnica,
    tamanhoTela: form.tamanhoTela,
    cenaRetratada: form.cenaRetratada,
    entregaObra: form.entregaObra,
  }
}

export function useNewEventPage({ onCreated }) {
  const [loading, setLoading] = useState(false)
  const [clientes, setClientes] = useState([])
  const [isNewClient, setIsNewClient] = useState(false)
  const [selectedClientId, setSelectedClientId] = useState('')
  const [form, setForm] = useState(EMPTY_EVENT_FORM)
  const { showToast, toast } = useToast()

  const showEventoAoVivoFields = usesEventoAoVivoFields(form.tipoServico)

  useEffect(() => {
    clientsService.list()
      .then(data => setClientes(Array.isArray(data) ? data : []))
      .catch(() => setClientes([]))
  }, [])

  const set = field => event => {
    const value = event.target.value
    setForm(prev => ({
      ...prev,
      [field]: value,
      ...(field === 'formaEntrega' && value === 'RETIRADA' ? { freteResponsavel: '' } : {}),
    }))
  }

  const setMasked = (field, maskFn) => event => {
    setForm(prev => ({ ...prev, [field]: maskFn(event.target.value) }))
  }

  const setCurrency = field => value => {
    setForm(prev => ({ ...prev, [field]: value }))
  }

  const setChecked = field => event => {
    setForm(prev => ({ ...prev, [field]: event.target.checked }))
  }

  const handleSubmit = async event => {
    event.preventDefault()

    if (!isNewClient && !selectedClientId) {
      showToast('Selecione um cliente existente ou crie um novo.', 'error')
      return
    }

    setLoading(true)
    try {
      let finalClientId = selectedClientId

      if (isNewClient) {
        const client = await clientsService.create({
          nome: form.clienteNome,
          email: form.clienteEmail,
          telefone: form.clienteTelefone,
          cpfCnpj: form.clienteCpf,
        })
        finalClientId = client.id
      }

      const createdEvent = await eventsService.create({
        clienteId: finalClientId,
        tipoServico: form.tipoServico,
        dadosServico: JSON.stringify(buildDadosServico(form)),
        tipoEvento: showEventoAoVivoFields ? form.tipoEvento : getFallbackTipoEvento(form.tipoServico),
        data: showEventoAoVivoFields ? form.data : todayISO(),
        horarioInicio: showEventoAoVivoFields ? form.horarioInicio || null : null,
        duracaoEstimada: showEventoAoVivoFields ? form.duracaoEstimada : null,
        local: showEventoAoVivoFields ? form.local : 'Não se aplica',
        numeroConvidados: showEventoAoVivoFields && form.numeroConvidados ? Number(form.numeroConvidados) : null,
        tecnica: showEventoAoVivoFields ? form.tecnica : null,
        tamanhoTela: showEventoAoVivoFields ? form.tamanhoTela : null,
        cenaRetratada: showEventoAoVivoFields ? form.cenaRetratada : null,
        entregaObra: showEventoAoVivoFields ? form.entregaObra : null,
        valor: parseCurrency(form.valor),
        valorSinal: parseCurrency(form.valorSinal),
        formaPagamentoSinal: form.formaPagamentoSinal,
        formaPagamentoRestante: form.formaPagamentoRestante,
        observacoes: nullableText(form.observacoes),
        contratoDispensado: form.contratoDispensado,
      })

      showToast('Evento criado com sucesso!', 'success')
      setTimeout(() => onCreated(createdEvent), 1200)
    } catch (err) {
      showToast(err.message || 'Erro ao salvar. Verifique se o servidor Java está rodando.', 'error')
    } finally {
      setLoading(false)
    }
  }

  return {
    clientes,
    form,
    handleSubmit,
    isNewClient,
    loading,
    selectedClientId,
    set,
    setClienteCpf: setMasked('clienteCpf', maskCpfCnpj),
    setClienteTelefone: setMasked('clienteTelefone', maskPhone),
    setChecked,
    setCurrency,
    setIsNewClient,
    setSelectedClientId,
    showEventoAoVivoFields,
    toast,
  }
}
