import { useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import TopBar from '../../../layouts/TopBar'
import SortableTh from '../../../shared/components/SortableTh'
import ConfirmDialog from '../../../shared/components/ConfirmDialog'
import { AlertTriangle, CalendarDays, CalendarPlus, CheckCircle2, CheckSquare, Clock, Columns3, ExternalLink, GripVertical, ListChecks, Printer, Trash2 } from 'lucide-react'
import { formatDateBR } from '../../../utils/date'
import { nextSort, sortItems } from '../../../utils/sort'
import TaskModal from '../components/TaskModal'
import { tasksService } from '../services/tasksService'
import { SERVICE_OPTIONS, STATUS_OPTIONS, useTasksPage } from '../hooks/useTasksPage'

const PAGE_SIZE = 8

const LIST_QUICK_FILTERS = [
  { key: 'pendentes', label: 'Pendentes' },
  { key: 'todas', label: 'Todas' },
  { key: 'concluidas', label: 'Concluídas' },
  { key: 'atrasadas', label: 'Atrasadas' },
]

function statusLabel(value) {
  return STATUS_OPTIONS.find(status => status.value === value)?.label || value
}

function serviceLabel(value) {
  return SERVICE_OPTIONS.find(service => service.value === value)?.label || 'Geral'
}

function taskItemsProgress(task) {
  const items = task?.checklistItems || []
  if (!items.length) return ''
  return `${items.filter(item => item.concluido).length}/${items.length}`
}

function parseDate(value) {
  if (!value) return null
  const [year, month, day] = String(value).split('-').map(Number)
  const date = new Date(year, month - 1, day)
  date.setHours(0, 0, 0, 0)
  return Number.isNaN(date.getTime()) ? null : date
}

function effectiveDate(task) {
  return parseDate(task.prazoDataFim || task.prazoData)
}

function isOverdue(task) {
  if (task.status === 'CONCLUIDA') return false
  const due = effectiveDate(task)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return due && due < today
}

function isDueSoon(task) {
  if (task.status === 'CONCLUIDA' || isOverdue(task)) return false
  const due = effectiveDate(task)
  if (!due) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  return due >= today && due <= tomorrow
}

function taskVisualStateClass(task) {
  if (isOverdue(task)) return 'task-overdue-pulse'
  if (isDueSoon(task)) return 'task-due-soon'
  return ''
}

function finalDeadlineLabel(task) {
  const date = formatDateBR(task.prazoDataFim || task.prazoData)
  if (!task.prazoHorarioFim) return date
  return `${date}, ${String(task.prazoHorarioFim).slice(0, 5)}`
}

function eventLabel(event) {
  if (!event?.clienteNome) return ''
  const date = formatDateBR(event.data)
  return date ? `${event.clienteNome} - ${date}` : event.clienteNome
}

function addDays(dateText, days) {
  if (!dateText) return ''
  const [year, month, day] = dateText.split('-').map(Number)
  const date = new Date(year, month - 1, day)
  date.setDate(date.getDate() + days)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function timeFromDate(date, fallback = '09:00') {
  if (!date || Number.isNaN(date.getTime())) return fallback
  const hours = date.getHours()
  const minutes = date.getMinutes()
  if (hours === 0 && minutes === 0) return fallback
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
}

function addHoursToTime(date, hours, fallback = '10:00') {
  if (!date || Number.isNaN(date.getTime())) return fallback
  if (date.getHours() === 0 && date.getMinutes() === 0) return fallback
  const next = new Date(date)
  next.setHours(next.getHours() + hours)
  return `${String(next.getHours()).padStart(2, '0')}:${String(next.getMinutes()).padStart(2, '0')}`
}

const STATUS_DOTS = {
  A_FAZER: 'status-dot-todo',
  EM_ANDAMENTO: 'status-dot-progress',
  CONCLUIDA: 'status-dot-done',
}

function TaskCalendarCard({ task }) {
  const labels = task.etiquetas || []
  const progress = taskItemsProgress(task)
  return (
    <div className={`task-calendar-card ${taskVisualStateClass(task)}`}>
      <div className="task-calendar-card-top">
        <span className={`task-status-dot ${STATUS_DOTS[task.status] || STATUS_DOTS.A_FAZER}`} />
        <strong>{task.titulo}</strong>
      </div>
      <div className="task-calendar-meta">
        <span>{serviceLabel(task.tipoServico)}</span>
        {task.event?.clienteNome && <span>{eventLabel(task.event)}</span>}
        {progress && <span>{progress} itens a realizar</span>}
      </div>
      {labels.length > 0 && (
        <div className="task-calendar-tags">
          {labels.slice(0, 3).map(label => <span key={label}>{label}</span>)}
          {labels.length > 3 && <span>+{labels.length - 3}</span>}
        </div>
      )}
    </div>
  )
}

function CalendarLegends() {
  return (
    <div className="tasks-legends">
      <div className="tasks-legend-group">
        <strong>Serviços</strong>
        {[...SERVICE_OPTIONS, { value: 'GERAL', label: 'Geral' }].map(service => (
          <span key={service.value} className="tasks-legend-item">
            <i className={`tasks-legend-swatch task-legend-${service.value}`} />
            {service.label}
          </span>
        ))}
      </div>
      <div className="tasks-legend-group">
        <strong>Status</strong>
        {STATUS_OPTIONS.map(status => (
          <span key={status.value} className="tasks-legend-item">
            <i className={`task-status-dot ${STATUS_DOTS[status.value]}`} />
            {status.label}
          </span>
        ))}
      </div>
    </div>
  )
}

function TaskKanbanCard({ task, onOpen, onDragStart }) {
  const labels = task.etiquetas || []
  const progress = taskItemsProgress(task)
  return (
    <button
      type="button"
      className={`task-kanban-card task-status-border-${task.status} ${taskVisualStateClass(task)}`}
      draggable
      onClick={() => onOpen(task)}
      onDragStart={event => onDragStart(event, task)}
    >
      <div className="task-kanban-card-head">
        <span className={`task-status-dot ${STATUS_DOTS[task.status] || STATUS_DOTS.A_FAZER}`} />
        <strong>{task.titulo}</strong>
        <GripVertical size={14} />
      </div>
      <span className={`tasks-service-pill task-service-text-${task.tipoServico || 'GERAL'}`}>{serviceLabel(task.tipoServico)}</span>
      <div className="task-kanban-meta">
        {task.event?.clienteNome && <span>{eventLabel(task.event)}</span>}
        <span>{finalDeadlineLabel(task)}</span>
        {progress && <span>{progress} itens a realizar</span>}
      </div>
      {labels.length > 0 && (
        <div className="tasks-row-tags">
          {labels.slice(0, 3).map(label => <span key={label}>{label}</span>)}
          {labels.length > 3 && <span>+{labels.length - 3}</span>}
        </div>
      )}
    </button>
  )
}

export default function TasksPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [activeView, setActiveView] = useState('list')
  const [calendarPdfParams, setCalendarPdfParams] = useState({ view: 'dayGridMonth', date: new Date().toISOString().slice(0, 10) })
  const [currentPage, setCurrentPage] = useState(1)
  const [deleteCandidate, setDeleteCandidate] = useState(null)
  const [draggedTaskId, setDraggedTaskId] = useState(null)
  const [sortConfig, setSortConfig] = useState({ key: 'prazo', direction: 'asc' })
  const [listFilter, setListFilter] = useState('pendentes')
  const {
    addChecklistItem,
    addComment,
    calendarEvents,
    changeStatus,
    commentText,
    deleteAttachment,
    deleteComment,
    deleteTask,
    events,
    filters,
    form,
    modalOpen,
    openCreate,
    openTask,
    removeChecklistItem,
    saveTask,
    saving,
    selectedTask,
    setCommentText,
    setField,
    setFilters,
    setModalOpen,
    tasks,
    toast,
    updateChecklistItem,
    updateDeadlineFromDrop,
    uploadAttachment,
  } = useTasksPage(searchParams.get('eventId') || '')

  const downloadCalendarPdf = () => {
    window.open(tasksService.calendarPdfUrl({
      ...filters,
      ...calendarPdfParams,
    }), '_blank', 'noopener,noreferrer')
  }

  const taskMetrics = useMemo(() => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    return {
      total: tasks.length,
      pending: tasks.filter(task => task.status !== 'CONCLUIDA').length,
      done: tasks.filter(task => task.status === 'CONCLUIDA').length,
      overdue: tasks.filter(isOverdue).length,
    }
  }, [tasks])

  // Lista, ordenação ou filtro novos voltam para a primeira página (ajuste na renderização, sem efeito)
  const [pageResetKey, setPageResetKey] = useState({ count: tasks.length, sortConfig, listFilter })
  if (pageResetKey.count !== tasks.length || pageResetKey.sortConfig !== sortConfig || pageResetKey.listFilter !== listFilter) {
    setPageResetKey({ count: tasks.length, sortConfig, listFilter })
    setCurrentPage(1)
  }

  const sortedTasks = useMemo(() => sortItems(tasks, sortConfig, {
    titulo: { value: task => task.titulo, type: 'text' },
    evento: { value: task => eventLabel(task.event), type: 'text' },
    prazo: { value: task => task.prazoDataFim || task.prazoData, type: 'date' },
    servico: { value: task => serviceLabel(task.tipoServico), type: 'text' },
    status: { value: task => isOverdue(task) ? 'Atrasado' : statusLabel(task.status), type: 'text' },
  }), [sortConfig, tasks])

  const listFilteredTasks = useMemo(() => sortedTasks.filter(task => {
    if (listFilter === 'pendentes') return task.status !== 'CONCLUIDA'
    if (listFilter === 'concluidas') return task.status === 'CONCLUIDA'
    if (listFilter === 'atrasadas') return isOverdue(task)
    return true
  }), [sortedTasks, listFilter])

  const totalPages = Math.max(Math.ceil(listFilteredTasks.length / PAGE_SIZE), 1)
  const safePage = Math.min(currentPage, totalPages)
  const pageStart = (safePage - 1) * PAGE_SIZE
  const paginatedTasks = listFilteredTasks.slice(pageStart, pageStart + PAGE_SIZE)

  const handleSort = key => {
    setSortConfig(current => nextSort(current, key))
  }

  const upcomingTasks = useMemo(() => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    return sortedTasks
      .filter(task => task.status !== 'CONCLUIDA' && effectiveDate(task) && effectiveDate(task) >= today)
      .slice(0, 5)
  }, [sortedTasks])

  const visibleCalendarEvents = useMemo(() => {
    return calendarEvents
  }, [calendarEvents])

  const kanbanTasksByStatus = useMemo(() => {
    return STATUS_OPTIONS.reduce((acc, status) => {
      acc[status.value] = sortedTasks.filter(task => task.status === status.value)
      return acc
    }, {})
  }, [sortedTasks])

  const confirmDeleteTask = async () => {
    const task = deleteCandidate
    setDeleteCandidate(null)
    await deleteTask(task)
  }

  const handleKanbanDragStart = (event, task) => {
    setDraggedTaskId(task.id)
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', String(task.id))
  }

  const handleKanbanDrop = async (event, status) => {
    event.preventDefault()
    const taskId = Number(event.dataTransfer.getData('text/plain') || draggedTaskId)
    setDraggedTaskId(null)
    const task = tasks.find(item => item.id === taskId)
    if (!task || task.status === status) return
    await changeStatus(task, status)
  }

  return (
    <>
      <TopBar title="Tarefas" />
      <div className="page-content tasks-page">
        <div className="page-header">
          <div className="page-header-left">
            <h1>Tarefas</h1>
            <p>Agenda operacional com tarefas avulsas e tarefas vinculadas aos eventos.</p>
          </div>
          <div className="tasks-header-actions">
            <div className="tasks-view-toggle">
              <button className={activeView === 'list' ? 'active' : ''} onClick={() => setActiveView('list')}>
                <ListChecks size={14} /> Lista
              </button>
              <button className={activeView === 'kanban' ? 'active' : ''} onClick={() => setActiveView('kanban')}>
                <Columns3 size={14} /> Kanban
              </button>
              <button className={activeView === 'calendar' ? 'active' : ''} onClick={() => setActiveView('calendar')}>
                <CalendarDays size={14} /> Calendário
              </button>
            </div>
            {activeView === 'calendar' && (
              <button className="btn btn-secondary" onClick={downloadCalendarPdf}>
              <Printer size={15} /> Baixar PDF
              </button>
            )}
            <button className="btn btn-primary" onClick={() => openCreate()}>
              <CalendarPlus size={15} /> Nova tarefa
            </button>
          </div>
        </div>

        {activeView === 'list' ? (
          <>
            <div className="tasks-summary-grid">
              <div className="tasks-summary-card">
                <span className="tasks-summary-icon summary-total"><ListChecks size={17} /></span>
                <small>Total</small>
                <strong>{taskMetrics.total}</strong>
                <em>Tarefas</em>
              </div>
              <div className="tasks-summary-card">
                <span className="tasks-summary-icon summary-pending"><Clock size={17} /></span>
                <small>Pendente</small>
                <strong>{taskMetrics.pending}</strong>
                <em>Aguardando ação</em>
              </div>
              <div className="tasks-summary-card">
                <span className="tasks-summary-icon summary-done"><CheckCircle2 size={17} /></span>
                <small>Concluído</small>
                <strong>{taskMetrics.done}</strong>
                <em>Finalizadas</em>
              </div>
              <div className="tasks-summary-card">
                <span className="tasks-summary-icon summary-overdue"><AlertTriangle size={17} /></span>
                <small>Atrasado</small>
                <strong>{taskMetrics.overdue}</strong>
                <em>Exigem atenção</em>
              </div>
            </div>

            <div className="table-toolbar">
              <div className="toolbar-inline">
                <div className="tabs tabs-inline">
                  {LIST_QUICK_FILTERS.map(tab => (
                    <button key={tab.key} className={`tab-btn ${listFilter === tab.key ? 'active' : ''}`} onClick={() => setListFilter(tab.key)}>
                      {tab.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            <div className="tasks-list-layout">
              <div className="tasks-table-card">
                <table className="tasks-table">
                  <thead>
                    <tr>
                      <SortableTh sortKey="titulo" sortConfig={sortConfig} onSort={handleSort}>Título da tarefa</SortableTh>
                      <SortableTh sortKey="evento" sortConfig={sortConfig} onSort={handleSort}>Evento relacionado</SortableTh>
                      <SortableTh sortKey="prazo" sortConfig={sortConfig} onSort={handleSort}>Prazo</SortableTh>
                      <SortableTh sortKey="servico" sortConfig={sortConfig} onSort={handleSort}>Serviço</SortableTh>
                      <SortableTh sortKey="status" sortConfig={sortConfig} onSort={handleSort}>Status</SortableTh>
                      <th>Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {paginatedTasks.map(task => (
                      <tr key={task.id} className={taskVisualStateClass(task)}>
                        <td>
                          <button className="tasks-title-link" onClick={() => openTask(task)}>{task.titulo}</button>
                          {task.etiquetas?.length > 0 && (
                            <div className="tasks-row-tags">
                              {task.etiquetas.slice(0, 3).map(label => <span key={label}>{label}</span>)}
                            </div>
                          )}
                        </td>
                        <td>
                          {task.event?.id ? (
                            <button className="tasks-event-link" onClick={() => navigate(`/eventos/${task.event.id}`)}>
                              <span>{eventLabel(task.event)}</span>
                              <ExternalLink size={12} />
                            </button>
                          ) : '-'}
                        </td>
                        <td>{finalDeadlineLabel(task)}</td>
                        <td><span className={`tasks-service-pill task-service-text-${task.tipoServico || 'GERAL'}`}>{serviceLabel(task.tipoServico)}</span></td>
                        <td><span className={`tasks-status-pill ${isOverdue(task) ? 'overdue' : task.status}`}>{isOverdue(task) ? 'Atrasado' : statusLabel(task.status)}</span></td>
                        <td>
                          <div className="action-btns">
                            <button className="action-btn" title="Editar tarefa" onClick={() => openTask(task)}>
                              <ListChecks size={14} />
                            </button>
                            {task.status !== 'CONCLUIDA' && (
                              <button className="action-btn action-btn-success" title="Concluir tarefa" onClick={() => changeStatus(task, 'CONCLUIDA')}>
                                <CheckSquare size={14} />
                              </button>
                            )}
                            <button className="action-btn action-btn-danger" title="Excluir tarefa" onClick={() => setDeleteCandidate(task)}>
                              <Trash2 size={14} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                    {listFilteredTasks.length === 0 && (
                      <tr>
                        <td colSpan={6}>
                          <div className="empty-state empty-state-compact">
                            <p>Nenhuma tarefa encontrada.</p>
                          </div>
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
                <div className="pagination">
                  <span>
                    Mostrando {listFilteredTasks.length > 0 ? pageStart + 1 : 0}-{Math.min(pageStart + PAGE_SIZE, listFilteredTasks.length)} de {listFilteredTasks.length} tarefas
                  </span>
                  <div className="pagination-pages">
                    <button className="page-btn" onClick={() => setCurrentPage(page => Math.max(page - 1, 1))} disabled={safePage === 1}>
                      ‹
                    </button>
                    {Array.from({ length: totalPages }, (_, index) => index + 1).map(page => (
                      <button
                        key={page}
                        className={`page-btn ${safePage === page ? 'active' : ''}`}
                        onClick={() => setCurrentPage(page)}
                      >
                        {page}
                      </button>
                    ))}
                    <button className="page-btn" onClick={() => setCurrentPage(page => Math.min(page + 1, totalPages))} disabled={safePage === totalPages}>
                      ›
                    </button>
                  </div>
                </div>
              </div>

              <div className="tasks-timeline-card">
                <div className="tasks-panel-title">
                  <Clock size={16} />
                  <strong>Timeline de próximos prazos</strong>
                </div>
                <div className="tasks-timeline-list">
                  {upcomingTasks.map(task => (
                    <button key={task.id} className="tasks-timeline-item" onClick={() => openTask(task)}>
                      <span className={`task-status-dot ${STATUS_DOTS[task.status] || STATUS_DOTS.A_FAZER}`} />
                      <em>{finalDeadlineLabel(task)}</em>
                      <strong>{task.titulo}</strong>
                      <small>{task.event?.clienteNome ? `Evento: ${task.event.clienteNome}` : 'Tarefa avulsa'}</small>
                    </button>
                  ))}
                  {upcomingTasks.length === 0 && <p className="text-secondary">Nenhum prazo futuro nos filtros atuais.</p>}
                </div>
              </div>
            </div>
          </>
        ) : activeView === 'kanban' ? (
          <>
            <div className="tasks-toolbar">
              <select className="form-select" value={filters.status} onChange={event => setFilters(current => ({ ...current, status: event.target.value }))}>
                <option value="">Todos os status</option>
                {STATUS_OPTIONS.map(status => <option key={status.value} value={status.value}>{status.label}</option>)}
              </select>
              <select className="form-select" value={filters.tipoServico} onChange={event => setFilters(current => ({ ...current, tipoServico: event.target.value }))}>
                <option value="">Todos os serviços</option>
                {SERVICE_OPTIONS.map(service => <option key={service.value} value={service.value}>{service.label}</option>)}
              </select>
              <select className="form-select" value={filters.eventId} onChange={event => setFilters(current => ({ ...current, eventId: event.target.value }))}>
                <option value="">Todos os eventos</option>
                {events.map(evento => (
                  <option key={evento.id} value={evento.id}>{eventLabel({ clienteNome: evento.client?.nome, data: evento.data }) || `Evento ${evento.id}`}</option>
                ))}
              </select>
              <input
                className="form-input"
                value={filters.label}
                onChange={event => setFilters(current => ({ ...current, label: event.target.value }))}
                placeholder="Filtrar por etiqueta"
              />
            </div>

            <div className="tasks-kanban-board">
              {STATUS_OPTIONS.map(status => {
                const columnTasks = kanbanTasksByStatus[status.value] || []
                return (
                  <section
                    key={status.value}
                    className={`tasks-kanban-column tasks-kanban-${status.value}`}
                    onDragOver={event => event.preventDefault()}
                    onDrop={event => handleKanbanDrop(event, status.value)}
                  >
                    <div className="tasks-kanban-column-header">
                      <span className={`task-status-dot ${STATUS_DOTS[status.value]}`} />
                      <strong>{status.label}</strong>
                      <em>{columnTasks.length}</em>
                    </div>
                    <div className="tasks-kanban-list">
                      {columnTasks.map(task => (
                        <TaskKanbanCard
                          key={task.id}
                          task={task}
                          onOpen={openTask}
                          onDragStart={handleKanbanDragStart}
                        />
                      ))}
                      {columnTasks.length === 0 && (
                        <div className="tasks-kanban-empty">
                          Solte uma tarefa aqui para alterar o status.
                        </div>
                      )}
                    </div>
                  </section>
                )
              })}
            </div>
          </>
        ) : (
          <>
            <div className="tasks-toolbar">
              <select className="form-select" value={filters.status} onChange={event => setFilters(current => ({ ...current, status: event.target.value }))}>
                <option value="">Todos os status</option>
                {STATUS_OPTIONS.map(status => <option key={status.value} value={status.value}>{status.label}</option>)}
              </select>
              <select className="form-select" value={filters.tipoServico} onChange={event => setFilters(current => ({ ...current, tipoServico: event.target.value }))}>
                <option value="">Todos os serviços</option>
                {SERVICE_OPTIONS.map(service => <option key={service.value} value={service.value}>{service.label}</option>)}
              </select>
              <select className="form-select" value={filters.eventId} onChange={event => setFilters(current => ({ ...current, eventId: event.target.value }))}>
                <option value="">Todos os eventos</option>
                {events.map(evento => (
                  <option key={evento.id} value={evento.id}>{eventLabel({ clienteNome: evento.client?.nome, data: evento.data }) || `Evento ${evento.id}`}</option>
                ))}
              </select>
              <input
                className="form-input"
                value={filters.label}
                onChange={event => setFilters(current => ({ ...current, label: event.target.value }))}
                placeholder="Filtrar por etiqueta"
              />
            </div>

            <CalendarLegends />

            <div className="tasks-calendar-shell">
              <FullCalendar
                plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
                initialView="dayGridMonth"
                locale="pt-br"
                firstDay={1}
                height="auto"
                editable
                selectable
                nowIndicator
                events={visibleCalendarEvents}
                allDaySlot={false}
                slotMinTime="00:00:00"
                slotMaxTime="24:00:00"
                nextDayThreshold="00:00:00"
                headerToolbar={{
                  left: 'prev,next today',
                  center: 'title',
                  right: 'dayGridMonth,timeGridWeek,timeGridDay',
                }}
                buttonText={{
                  today: 'Hoje',
                  month: 'Mês',
                  week: 'Semana',
                  day: 'Dia',
                }}
                dateClick={info => openCreate({
                  prazoData: info.dateStr.slice(0, 10),
                  prazoDataFim: info.dateStr.slice(0, 10),
                  prazoHorario: timeFromDate(info.date),
                  prazoHorarioFim: addHoursToTime(info.date, 1),
                })}
                eventClick={info => openTask(info.event.extendedProps.task)}
                eventContent={info => <TaskCalendarCard task={info.event.extendedProps.task} />}
                eventDrop={updateDeadlineFromDrop}
                eventResize={updateDeadlineFromDrop}
                datesSet={info => setCalendarPdfParams({
                  view: info.view.type,
                  date: info.view.currentStart.toISOString().slice(0, 10),
                  start: info.startStr.slice(0, 10),
                  end: addDays(info.endStr.slice(0, 10), -1),
                })}
              />
            </div>
          </>
        )}
      </div>

      <TaskModal
        addChecklistItem={addChecklistItem}
        addComment={addComment}
        commentText={commentText}
        deleteAttachment={deleteAttachment}
        deleteComment={deleteComment}
        events={events}
        form={form}
        modalOpen={modalOpen}
        onDeleteTask={setDeleteCandidate}
        saveTask={saveTask}
        saving={saving}
        selectedTask={selectedTask}
        setCommentText={setCommentText}
        setField={setField}
        setModalOpen={setModalOpen}
        updateChecklistItem={updateChecklistItem}
        removeChecklistItem={removeChecklistItem}
        uploadAttachment={uploadAttachment}
      />

      <ConfirmDialog
        open={Boolean(deleteCandidate)}
        title="Excluir tarefa?"
        description="Essa ação remove a tarefa, seus comentários, checklist, etiquetas e anexos. Essa operação não pode ser desfeita."
        confirmLabel="Excluir tarefa"
        tone="danger"
        onCancel={() => setDeleteCandidate(null)}
        onConfirm={confirmDeleteTask}
      />

      {toast && <div className={`toast ${toast.type}`}>{toast.msg}</div>}

      <div className="tasks-print-summary">
        {tasks.map(task => (
          <div key={task.id} className="tasks-print-item">
            <strong>{task.titulo}</strong>
            <span><Clock size={12} /> {formatDateBR(task.prazoData)} {task.prazoHorario ? String(task.prazoHorario).slice(0, 5) : ''}</span>
            <span>{statusLabel(task.status)} - {serviceLabel(task.tipoServico)} - {taskItemsProgress(task)} itens a realizar</span>
          </div>
        ))}
      </div>
    </>
  )
}
