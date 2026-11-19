import React, { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AlertTriangle, Bell, CalendarClock, CheckCircle2, ClipboardList, Clock, FileWarning, Search, X } from 'lucide-react'
import { contractsService } from '../features/contracts/services/contractsService'
import { eventsService } from '../features/events/services/eventsService'
import { tasksService } from '../features/tasks/services/tasksService'

const DISMISSED_NOTIFICATIONS_KEY = 'docflow-dismissed-notifications'

function normalizeDate(value) {
  if (!value) return null
  const date = new Date(value)
  date.setHours(0, 0, 0, 0)
  return Number.isNaN(date.getTime()) ? null : date
}

function daysUntil(value) {
  const date = normalizeDate(value)
  if (!date) return null

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return Math.round((date.getTime() - today.getTime()) / 86400000)
}

export default function TopBar({ breadcrumb = [], title, searchValue = '', onSearchChange, searchPlaceholder = 'Buscar evento...' }) {
  const showSearch = typeof onSearchChange === 'function'
  const navigate = useNavigate()
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const [events, setEvents] = useState([])
  const [contracts, setContracts] = useState([])
  const [tasks, setTasks] = useState([])
  const [dismissedNotifications, setDismissedNotifications] = useState(() => {
    try {
      const stored = JSON.parse(localStorage.getItem(DISMISSED_NOTIFICATIONS_KEY) || '[]')
      return new Set(Array.isArray(stored) ? stored : [])
    } catch {
      return new Set()
    }
  })

  useEffect(() => {
    Promise.all([
      eventsService.list(),
      contractsService.list(),
      tasksService.list(),
    ])
      .then(([eventsData, contractsData, tasksData]) => {
        setEvents(Array.isArray(eventsData) ? eventsData : [])
        setContracts(Array.isArray(contractsData) ? contractsData : [])
        setTasks(Array.isArray(tasksData) ? tasksData : [])
      })
      .catch(() => {
        setEvents([])
        setContracts([])
        setTasks([])
      })
  }, [])

  const notifications = useMemo(() => {
    const contractsByEvent = new Map(contracts.map(contract => [contract.event?.id, contract]))
    const isDismissed = key => dismissedNotifications.has(key)

    const generatedContracts = contracts
      .filter(contract => (contract.status || '').toUpperCase() === 'RASCUNHO')
      .filter(contract => !isDismissed(`contract-generated-${contract.id}-RASCUNHO`))
    const pendingContracts = contracts
      .filter(contract => (contract.status || '').toUpperCase() === 'ENVIADO')
      .filter(contract => !isDismissed(`contract-pending-${contract.id}-ENVIADO`))
    const signedContracts = contracts
      .filter(contract => (contract.status || '').toUpperCase() === 'ASSINADO')
      .filter(contract => !isDismissed(`contract-signed-${contract.id}-ASSINADO`))
    const eventsWithoutContract = events
      .filter(event => !contractsByEvent.has(event.id))
      .filter(event => !isDismissed(`event-no-contract-${event.id}`))
    const missingPdfContracts = contracts
      .filter(contract => !contract.pdfUrl)
      .filter(contract => !isDismissed(`contract-missing-pdf-${contract.id}`))
    const upcomingEvents = events
      .map(event => ({ event, days: daysUntil(event.data) }))
      .filter(item => item.days !== null && item.days >= 0 && item.days <= 7)
      .filter(item => !isDismissed(`event-upcoming-${item.event.id}-${item.event.data}`))
      .sort((a, b) => a.days - b.days)
    const upcomingTasks = tasks
      .map(task => ({ task, days: daysUntil(task.prazoDataFim || task.prazoData) }))
      .filter(item => item.task.status !== 'CONCLUIDA' && item.days !== null && item.days >= 0 && item.days <= 7)
      .filter(item => !isDismissed(`task-upcoming-${item.task.id}-${item.task.prazoDataFim || item.task.prazoData}`))
      .sort((a, b) => a.days - b.days)
    const visibleKeys = [
      ...eventsWithoutContract.map(event => `event-no-contract-${event.id}`),
      ...generatedContracts.map(contract => `contract-generated-${contract.id}-RASCUNHO`),
      ...pendingContracts.map(contract => `contract-pending-${contract.id}-ENVIADO`),
      ...upcomingEvents.map(({ event }) => `event-upcoming-${event.id}-${event.data}`),
      ...upcomingTasks.map(({ task }) => `task-upcoming-${task.id}-${task.prazoDataFim || task.prazoData}`),
      ...signedContracts.map(contract => `contract-signed-${contract.id}-ASSINADO`),
      ...missingPdfContracts.map(contract => `contract-missing-pdf-${contract.id}`),
    ]
    return {
      pendingContracts,
      signedContracts,
      eventsWithoutContract,
      missingPdfContracts,
      generatedContracts,
      upcomingEvents,
      upcomingTasks,
      visibleKeys,
      summary: {
        generated: generatedContracts.length,
        pending: pendingContracts.length,
        signed: signedContracts.length,
        noContract: eventsWithoutContract.length,
        tasks: upcomingTasks.length,
      },
      count: generatedContracts.length + pendingContracts.length + upcomingEvents.length + upcomingTasks.length + eventsWithoutContract.length + missingPdfContracts.length + signedContracts.length,
    }
  }, [contracts, events, tasks, dismissedNotifications])

  const persistDismissedNotifications = nextSet => {
    localStorage.setItem(DISMISSED_NOTIFICATIONS_KEY, JSON.stringify([...nextSet]))
    setDismissedNotifications(nextSet)
  }

  const dismissNotification = key => {
    const next = new Set(dismissedNotifications)
    next.add(key)
    persistDismissedNotifications(next)
  }

  const clearVisibleNotifications = () => {
    const next = new Set(dismissedNotifications)
    notifications.visibleKeys.forEach(key => next.add(key))
    persistDismissedNotifications(next)
  }

  const goTo = path => {
    setNotificationsOpen(false)
    navigate(path)
  }

  return (
    <header className="topbar">
      <div className="topbar-left">
        <div className="topbar-breadcrumb">
          {breadcrumb.map((crumb, i) => (
            <React.Fragment key={i}>
              {i > 0 && <span className="breadcrumb-separator">/</span>}
              <a href={crumb.href || '#'}>{crumb.label}</a>
            </React.Fragment>
          ))}
          {title && (
            <>
              {breadcrumb.length > 0 && <span className="breadcrumb-separator">/</span>}
              <span>{title}</span>
            </>
          )}
        </div>
      </div>

      <div className="topbar-right">
        {showSearch && (
          <div className="search-box">
            <Search />
            <input
              placeholder={searchPlaceholder}
              value={searchValue}
              onChange={e => onSearchChange(e.target.value)}
            />
          </div>
        )}

        <div className="notification-center">
          <button
            className="topbar-icon-btn"
            onClick={() => setNotificationsOpen(current => !current)}
            title="Notificações"
          >
            <Bell size={16} />
            {notifications.count > 0 && (
              <span className="notification-count">{Math.min(notifications.count, 99)}</span>
            )}
          </button>

          {notificationsOpen && (
            <div className="notification-panel">
              <div className="notification-panel-header">
                <div>
                  <strong>Central de notificações</strong>
                  <span>Pendências e avisos que você ainda não limpou</span>
                </div>
                <div className="notification-header-actions">
                  {notifications.visibleKeys.length > 0 && (
                    <button type="button" onClick={clearVisibleNotifications}>Limpar tudo</button>
                  )}
                </div>
              </div>

              <div className="notification-summary-grid">
                <SummaryItem label="Sem contrato" value={notifications.summary.noContract} />
                <SummaryItem label="Gerados" value={notifications.summary.generated} />
                <SummaryItem label="Assinatura" value={notifications.summary.pending} />
                <SummaryItem label="Assinados" value={notifications.summary.signed} />
                <SummaryItem label="Tarefas" value={notifications.summary.tasks} />
              </div>

              <NotificationSection
                title="Sem contrato"
                icon={FileWarning}
                empty="Nenhum evento sem contrato."
                items={notifications.eventsWithoutContract.slice(0, 4).map(event => ({
                  key: `event-no-contract-${event.id}`,
                  title: event.client?.nome || 'Cliente',
                  detail: 'Evento ainda sem contrato',
                  onClick: () => goTo(`/contratos/gerar?eventId=${event.id}`),
                  onDismiss: () => dismissNotification(`event-no-contract-${event.id}`),
                }))}
              />

              <NotificationSection
                title="Pendentes de envio para Autentique"
                icon={FileWarning}
                empty="Nenhum contrato aguardando envio."
                items={notifications.generatedContracts.slice(0, 4).map(contract => ({
                  key: `contract-generated-${contract.id}-RASCUNHO`,
                  title: contract.event?.client?.nome || 'Cliente',
                  detail: `Enviar para Autentique - ${contract.event?.tipoEvento || 'Evento'}`,
                  onClick: () => goTo(`/eventos/${contract.event?.id}`),
                  onDismiss: () => dismissNotification(`contract-generated-${contract.id}-RASCUNHO`),
                }))}
              />

              <NotificationSection
                title="Aguardando assinatura"
                icon={Clock}
                empty="Nenhum contrato aguardando ação."
                items={notifications.pendingContracts.slice(0, 4).map(contract => ({
                  key: `contract-pending-${contract.id}-ENVIADO`,
                  title: contract.event?.client?.nome || 'Cliente',
                  detail: `Pendente de assinatura - ${contract.event?.tipoEvento || 'Evento'}`,
                  onClick: () => goTo(`/eventos/${contract.event?.id}`),
                  onDismiss: () => dismissNotification(`contract-pending-${contract.id}-ENVIADO`),
                }))}
              />

              <NotificationSection
                title="Eventos próximos"
                icon={CalendarClock}
                empty="Nenhum evento nos próximos 7 dias."
                items={notifications.upcomingEvents.slice(0, 4).map(({ event, days }) => ({
                  key: `event-upcoming-${event.id}-${event.data}`,
                  title: event.client?.nome || 'Cliente',
                  detail: days === 0 ? 'Acontece hoje' : `Em ${days} dia${days > 1 ? 's' : ''}`,
                  onClick: () => goTo(`/eventos/${event.id}`),
                  onDismiss: () => dismissNotification(`event-upcoming-${event.id}-${event.data}`),
                }))}
              />

              <NotificationSection
                title="Tarefas próximas"
                icon={Clock}
                empty="Nenhuma tarefa nos próximos 7 dias."
                items={notifications.upcomingTasks.slice(0, 4).map(({ task, days }) => {
                  const dateKey = task.prazoDataFim || task.prazoData
                  return {
                    key: `task-upcoming-${task.id}-${dateKey}`,
                    title: task.titulo || 'Tarefa',
                    detail: `${days === 0 ? 'Prazo hoje' : `Em ${days} dia${days > 1 ? 's' : ''}`}${task.event?.clienteNome ? ` - ${task.event.clienteNome}` : ''}`,
                    onClick: () => goTo(task.event?.id ? `/tarefas?eventId=${task.event.id}` : '/tarefas'),
                    onDismiss: () => dismissNotification(`task-upcoming-${task.id}-${dateKey}`),
                  }
                })}
              />

              <NotificationSection
                title="Assinados"
                icon={CheckCircle2}
                empty="Nenhum contrato assinado ainda."
                items={notifications.signedContracts.slice(0, 3).map(contract => ({
                  key: `contract-signed-${contract.id}-ASSINADO`,
                  title: contract.event?.client?.nome || 'Cliente',
                  detail: contract.event?.tipoEvento || 'Contrato assinado',
                  onClick: () => goTo(`/eventos/${contract.event?.id}`),
                  onDismiss: () => dismissNotification(`contract-signed-${contract.id}-ASSINADO`),
                }))}
              />

              <NotificationSection
                title="Falhas operacionais"
                icon={AlertTriangle}
                empty="Nenhuma pendência operacional encontrada."
                items={notifications.missingPdfContracts.slice(0, 4).map(contract => ({
                    key: `contract-missing-pdf-${contract.id}`,
                    title: contract.event?.client?.nome || 'Cliente',
                    detail: 'Contrato sem PDF gerado',
                    onClick: () => goTo(`/eventos/${contract.event?.id}`),
                    onDismiss: () => dismissNotification(`contract-missing-pdf-${contract.id}`),
                  }))}
              />
            </div>
          )}
        </div>

        <div className="avatar">BL</div>
      </div>
    </header>
  )
}

function SummaryItem({ label, value }) {
  return (
    <div className="notification-summary-item">
      <strong>{value}</strong>
      <span>{label}</span>
    </div>
  )
}

function NotificationSection({ title, icon: Icon, empty, items }) {
  return (
    <section className="notification-section">
      <div className="notification-section-title">
        <Icon size={14} />
        {title}
      </div>

      {items.length > 0 ? (
        <div className="notification-list">
          {items.map(item => (
            <div key={item.key} className="notification-item-row">
              <button type="button" className="notification-item" onClick={item.onClick}>
                <Icon size={14} />
                <span>
                  <strong>{item.title}</strong>
                  <small>{item.detail}</small>
                </span>
              </button>
              <button
                type="button"
                className="notification-dismiss"
                onClick={item.onDismiss}
                title="Limpar notificação"
              >
                <X size={13} />
              </button>
            </div>
          ))}
        </div>
      ) : (
        <div className="notification-empty">
          <ClipboardList size={14} />
          {empty}
        </div>
      )}
    </section>
  )
}
