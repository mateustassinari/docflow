import { useCallback, useEffect, useMemo, useState } from 'react'
import { eventsService } from '../../events/services/eventsService'
import { financialService } from '../services/financialService'
import { parseLocalDate } from '../../../utils/date'
import { useToast } from '../../../shared/hooks/useToast'

const TIPO_LABELS = {
  PINTURA_AO_VIVO: 'Pintura ao Vivo',
  AQUARELA_AO_VIVO: 'Aquarela ao Vivo',
  ENCOMENDA_ARTISTICA: 'Encomenda Artística',
  IDENTIDADE_VISUAL: 'Identidade Visual',
}

const ALL_TIPOS = Object.keys(TIPO_LABELS)

function tipoLabel(value) {
  return TIPO_LABELS[value] || value || 'Não definido'
}

function effectiveCost(evento, costDefaults) {
  const custoPadrao = evento.custoTotal != null
    ? Number(evento.custoTotal)
    : (costDefaults[evento.tipoServico || 'PINTURA_AO_VIVO'] ?? 0)
  const custosExtras = Number(evento.custosExtras) || 0
  return custoPadrao + custosExtras
}

export function useFinancialPage() {
  const [eventos, setEventos] = useState([])
  const [loading, setLoading] = useState(true)
  const [markingId, setMarkingId] = useState(null)
  const [costDefaults, setCostDefaults] = useState({})
  const [savingCostDefault, setSavingCostDefault] = useState(null)
  const { showToast, toast } = useToast()

  const now = new Date()
  const [periodoMes, setPeriodoMes] = useState(now.getMonth() + 1)
  const [periodoAno, setPeriodoAno] = useState(now.getFullYear())
  const [mostrarTodos, setMostrarTodos] = useState(true)

  useEffect(() => {
    setLoading(true)
    Promise.all([
      eventsService.list(),
      financialService.getCostDefaults(),
    ])
      .then(([eventosData, defaultsData]) => {
        setEventos(Array.isArray(eventosData) ? eventosData : [])
        const map = {}
        if (Array.isArray(defaultsData)) {
          defaultsData.forEach(d => { map[d.tipoServico] = Number(d.custoDefault) || 0 })
        }
        setCostDefaults(map)
      })
      .catch(() => showToast('Erro ao carregar dados financeiros', 'error'))
      .finally(() => setLoading(false))
  }, [showToast])

  const anosDisponiveis = useMemo(() => {
    const anos = new Set(eventos.map(e => {
      const d = parseLocalDate(e.data)
      return d ? d.getFullYear() : null
    }).filter(Boolean))
    return Array.from(anos).sort((a, b) => b - a)
  }, [eventos])

  const eventosFiltrados = useMemo(() => {
    if (mostrarTodos) return eventos
    return eventos.filter(e => {
      const d = parseLocalDate(e.data)
      return d && d.getMonth() + 1 === periodoMes && d.getFullYear() === periodoAno
    })
  }, [eventos, mostrarTodos, periodoMes, periodoAno])

  const receitaTotal = useMemo(() =>
    eventosFiltrados.reduce((sum, e) => sum + (Number(e.valor) || 0), 0),
    [eventosFiltrados])

  const receitaRecebida = useMemo(() =>
    eventosFiltrados.reduce((sum, e) => {
      const sinalPart = e.sinalRecebido ? (Number(e.valorSinal) || 0) : 0
      const restantePart = e.restanteRecebido
        ? Math.max((Number(e.valor) || 0) - (Number(e.valorSinal) || 0), 0)
        : 0
      return sum + sinalPart + restantePart
    }, 0),
    [eventosFiltrados])

  const sinaisPendentes = useMemo(() =>
    eventosFiltrados.filter(e => !e.sinalRecebido && e.valorSinal),
    [eventosFiltrados])

  const valorSinaisPendentes = useMemo(() =>
    sinaisPendentes.reduce((sum, e) => sum + (Number(e.valorSinal) || 0), 0),
    [sinaisPendentes])

  const pagamentosFinaisPendentes = useMemo(() =>
    eventosFiltrados.filter(e => !e.restanteRecebido),
    [eventosFiltrados])

  const valorRestantesPendentes = useMemo(() =>
    pagamentosFinaisPendentes.reduce((sum, e) => {
      const sinalJaRecebido = e.sinalRecebido ? (Number(e.valorSinal) || 0) : 0
      const pendente = (Number(e.valor) || 0) - sinalJaRecebido
      return sum + Math.max(pendente, 0)
    }, 0),
    [pagamentosFinaisPendentes])

  const custoTotalPeriodo = useMemo(() =>
    eventosFiltrados.reduce((sum, e) => sum + effectiveCost(e, costDefaults), 0),
    [eventosFiltrados, costDefaults])

  const lucroTotal = useMemo(() =>
    receitaRecebida - custoTotalPeriodo,
    [receitaRecebida, custoTotalPeriodo])

  const breakdownServico = useMemo(() => {
    return ALL_TIPOS.map(tipo => {
      const group = eventosFiltrados.filter(e => (e.tipoServico || 'PINTURA_AO_VIVO') === tipo)
      const total = group.reduce((sum, e) => sum + (Number(e.valor) || 0), 0)
      const custoGrupo = group.reduce((sum, e) => sum + effectiveCost(e, costDefaults), 0)
      const custoMedio = group.length > 0 ? custoGrupo / group.length : 0
      const lucroMedio = group.length > 0 ? (total - custoGrupo) / group.length : 0
      return {
        tipo,
        label: tipoLabel(tipo),
        count: group.length,
        receitaTotal: total,
        ticketMedio: group.length > 0 ? total / group.length : 0,
        percentual: receitaTotal > 0 ? (total / receitaTotal) * 100 : 0,
        custoMedio,
        lucroMedio,
      }
    }).filter(item => item.count > 0)
  }, [eventosFiltrados, receitaTotal, costDefaults])

  const pagamentosPendentes = useMemo(() =>
    eventosFiltrados.filter(e => !e.sinalRecebido || !e.restanteRecebido),
    [eventosFiltrados])

  const pagamentosRecebidos = useMemo(() =>
    eventosFiltrados.filter(e => e.sinalRecebido || e.restanteRecebido),
    [eventosFiltrados])

  const handleMarkSinal = useCallback(async (evento) => {
    const key = `sinal-${evento.id}`
    setMarkingId(key)
    try {
      const today = new Date().toISOString().slice(0, 10)
      const updated = await eventsService.updatePayment(evento.id, {
        sinalRecebido: true,
        dataRecebimentoSinal: today,
        restanteRecebido: evento.restanteRecebido,
        dataRecebimentoRestante: evento.dataRecebimentoRestante || null,
        observacaoPagamento: evento.observacaoPagamento || null,
        custoTotal: evento.custoTotal ?? null,
        custosExtras: evento.custosExtras ?? null,
      })
      setEventos(prev => prev.map(e => e.id === updated.id ? updated : e))
      showToast('Sinal marcado como recebido!', 'success')
    } catch (err) {
      showToast(err.message || 'Erro ao atualizar pagamento', 'error')
    } finally {
      setMarkingId(null)
    }
  }, [showToast])

  const handleMarkRestante = useCallback(async (evento) => {
    const key = `restante-${evento.id}`
    setMarkingId(key)
    try {
      const today = new Date().toISOString().slice(0, 10)
      const updated = await eventsService.updatePayment(evento.id, {
        sinalRecebido: evento.sinalRecebido,
        dataRecebimentoSinal: evento.dataRecebimentoSinal || null,
        restanteRecebido: true,
        dataRecebimentoRestante: today,
        observacaoPagamento: evento.observacaoPagamento || null,
        custoTotal: evento.custoTotal ?? null,
        custosExtras: evento.custosExtras ?? null,
      })
      setEventos(prev => prev.map(e => e.id === updated.id ? updated : e))
      showToast('Pagamento final marcado como recebido!', 'success')
    } catch (err) {
      showToast(err.message || 'Erro ao atualizar pagamento', 'error')
    } finally {
      setMarkingId(null)
    }
  }, [showToast])

  const handleUpdateCostDefault = useCallback(async (tipoServico, value) => {
    setSavingCostDefault(tipoServico)
    try {
      const updated = await financialService.updateCostDefault(tipoServico, Number(value) || 0)
      setCostDefaults(prev => ({ ...prev, [updated.tipoServico]: Number(updated.custoDefault) || 0 }))
      showToast('Custo padrão atualizado!', 'success')
    } catch (err) {
      showToast(err.message || 'Erro ao atualizar custo padrão', 'error')
    } finally {
      setSavingCostDefault(null)
    }
  }, [showToast])

  return {
    anosDisponiveis,
    breakdownServico,
    costDefaults,
    custoTotalPeriodo,
    eventosFiltrados,
    loading,
    lucroTotal,
    markingId,
    mostrarTodos,
    pagamentosFinaisPendentes,
    pagamentosPendentes,
    pagamentosRecebidos,
    periodoAno,
    periodoMes,
    receitaRecebida,
    receitaTotal,
    savingCostDefault,
    setMostrarTodos,
    setPeriodoAno,
    setPeriodoMes,
    sinaisPendentes,
    toast,
    valorRestantesPendentes,
    valorSinaisPendentes,
    handleMarkSinal,
    handleMarkRestante,
    handleUpdateCostDefault,
    effectiveCost: (evento) => effectiveCost(evento, costDefaults),
  }
}
