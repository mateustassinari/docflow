import { useEffect, useState } from 'react'
import { aiService } from '../services/aiService'
import { useToast } from '../../../shared/hooks/useToast'

const SUGESTOES = [
  'Quanto recebi este mês?',
  'Tenho pagamentos pendentes?',
  'Quais tarefas estão atrasadas?',
  'Qual tipo de serviço eu faço mais?',
]

const MENSAGEM_INICIAL = {
  role: 'assistant',
  text: 'Olá! Sou seu assistente. Pergunte sobre seus eventos, finanças ou tarefas. 🎨',
}

export function useAiPage() {
  const { toast, showToast } = useToast()

  const [messages, setMessages] = useState([MENSAGEM_INICIAL])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)

  const [insights, setInsights] = useState([])
  const [insightsLoading, setInsightsLoading] = useState(true)
  const [insightsError, setInsightsError] = useState(false)

  const loadInsights = () => {
    setInsightsLoading(true)
    setInsightsError(false)
    aiService
      .getInsights()
      .then(data => setInsights(data?.insights || []))
      .catch(() => setInsightsError(true))
      .finally(() => setInsightsLoading(false))
  }

  useEffect(() => {
    loadInsights()
  }, [])

  const send = async text => {
    const question = (text ?? input).trim()
    if (!question || sending) return

    setInput('')
    setMessages(prev => [...prev, { role: 'user', text: question }])
    setSending(true)

    try {
      const data = await aiService.chat(question)
      setMessages(prev => [...prev, { role: 'assistant', text: data?.answer || '(sem resposta)' }])
    } catch (err) {
      const msg = err?.message || 'Não foi possível falar com o assistente.'
      setMessages(prev => [...prev, { role: 'assistant', text: `⚠️ ${msg}`, error: true }])
      showToast(msg, 'error')
    } finally {
      setSending(false)
    }
  }

  const handleSubmit = event => {
    event.preventDefault()
    send()
  }

  return {
    messages,
    input,
    setInput,
    sending,
    send,
    handleSubmit,
    insights,
    insightsLoading,
    insightsError,
    reloadInsights: loadInsights,
    sugestoes: SUGESTOES,
    toast,
  }
}
