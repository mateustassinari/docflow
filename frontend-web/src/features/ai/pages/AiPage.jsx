import { useEffect, useRef } from 'react'
import TopBar from '../../../layouts/TopBar'
import { Sparkles, Send, Bot, User, RefreshCw, AlertCircle, Loader2 } from 'lucide-react'
import { useAiPage } from '../hooks/useAiPage'

export default function AiPage() {
  const {
    messages,
    input,
    setInput,
    sending,
    send,
    handleSubmit,
    insights,
    insightsLoading,
    insightsError,
    reloadInsights,
    sugestoes,
    toast,
  } = useAiPage()

  const messagesEndRef = useRef(null)

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, sending])

  return (
    <>
      <TopBar title="Assistente IA" />
      <div className="page-content">
        <div className="page-header">
          <div className="page-header-left">
            <h1>Assistente IA</h1>
            <p className="page-subtitle">Pergunte sobre seus eventos, finanças e tarefas — tudo processado localmente.</p>
          </div>
        </div>

        <div className="ai-layout">
          {/* Insights */}
          <div className="card ai-insights-card">
            <div className="card-section-header">
              <h2 className="card-section-title">
                <Sparkles size={16} /> Insights da IA
              </h2>
              <button
                className="btn btn-ghost btn-link-primary"
                onClick={reloadInsights}
                disabled={insightsLoading}
                title="Gerar novamente"
              >
                <RefreshCw size={14} className={insightsLoading ? 'spin' : ''} /> Atualizar
              </button>
            </div>

            {insightsLoading ? (
              <div className="ai-insights-loading">
                <Loader2 size={16} className="spin" /> Analisando seus dados...
              </div>
            ) : insightsError ? (
              <div className="empty-state">
                <AlertCircle /> Assistente indisponível. Verifique se o Ollama está rodando.
              </div>
            ) : insights.length === 0 ? (
              <div className="empty-state">
                <AlertCircle /> Nenhum insight gerado.
              </div>
            ) : (
              <ul className="ai-insights-list">
                {insights.map((insight, i) => (
                  <li key={i}>{insight}</li>
                ))}
              </ul>
            )}
          </div>

          {/* Chat */}
          <div className="card ai-chat-card">
            <div className="card-section-header">
              <h2 className="card-section-title">
                <Bot size={16} /> Conversar
              </h2>
            </div>

            <div className="ai-chat-messages">
              {messages.map((msg, i) => (
                <div key={i} className={`ai-msg ai-msg-${msg.role} ${msg.error ? 'ai-msg-error' : ''}`}>
                  <div className="ai-msg-avatar">
                    {msg.role === 'user' ? <User size={14} /> : <Bot size={14} />}
                  </div>
                  <div className="ai-msg-bubble">{msg.text}</div>
                </div>
              ))}
              {sending && (
                <div className="ai-msg ai-msg-assistant">
                  <div className="ai-msg-avatar"><Bot size={14} /></div>
                  <div className="ai-msg-bubble ai-msg-typing">
                    <Loader2 size={14} className="spin" /> Pensando...
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>

            {messages.length <= 1 && (
              <div className="ai-suggestions">
                {sugestoes.map(s => (
                  <button key={s} type="button" className="ai-suggestion-chip" onClick={() => send(s)} disabled={sending}>
                    {s}
                  </button>
                ))}
              </div>
            )}

            <form className="ai-chat-input" onSubmit={handleSubmit}>
              <input
                type="text"
                placeholder="Digite sua pergunta..."
                value={input}
                onChange={e => setInput(e.target.value)}
                disabled={sending}
              />
              <button type="submit" className="btn btn-primary" disabled={sending || !input.trim()}>
                <Send size={16} />
              </button>
            </form>
          </div>
        </div>
      </div>

      {toast && <div className={`toast ${toast.type}`}>{toast.msg}</div>}
    </>
  )
}
