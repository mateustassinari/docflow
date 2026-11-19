import { useCallback, useEffect, useMemo, useState } from 'react'
import { useToast } from '../../../shared/hooks/useToast'
import { eventsService } from '../../events/services/eventsService'
import { tasksService } from '../services/tasksService'

const EMPTY_FORM = {
  eventId: '',
  titulo: '',
  descricao: '',
  prazoData: '',
  prazoDataFim: '',
  prazoHorario: '09:00',
  prazoHorarioFim: '10:00',
  status: 'A_FAZER',
  tipoServico: '',
  etiquetasText: '',
  checklistItems: [],
}

function toDateInput(date) {
  if (!date) return ''
  const safeDate = date instanceof Date ? date : new Date(date)
  if (Number.isNaN(safeDate.getTime())) return ''
  const year = safeDate.getFullYear()
  const month = String(safeDate.getMonth() + 1).padStart(2, '0')
  const day = String(safeDate.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function toTimeInput(date) {
  if (!date) return ''
  const safeDate = date instanceof Date ? date : new Date(date)
  if (Number.isNaN(safeDate.getTime())) return ''
  return `${String(safeDate.getHours()).padStart(2, '0')}:${String(safeDate.getMinutes()).padStart(2, '0')}`
}

function formFromTask(task) {
  return {
    eventId: task.event?.id ? String(task.event.id) : '',
    titulo: task.titulo || '',
    descricao: task.descricao || '',
    prazoData: task.prazoData || '',
    prazoDataFim: task.prazoDataFim || '',
    prazoHorario: task.prazoHorario ? String(task.prazoHorario).slice(0, 5) : '09:00',
    prazoHorarioFim: task.prazoHorarioFim ? String(task.prazoHorarioFim).slice(0, 5) : '10:00',
    status: task.status || 'A_FAZER',
    tipoServico: task.tipoServico || '',
    etiquetasText: (task.etiquetas || []).join(', '),
    checklistItems: (task.checklistItems || []).map(item => ({ ...item })),
  }
}

function payloadFromForm(form) {
  return {
    eventId: form.eventId ? Number(form.eventId) : null,
    titulo: form.titulo,
    descricao: form.descricao || null,
    prazoData: form.prazoData,
    prazoDataFim: form.prazoDataFim || null,
    prazoHorario: form.prazoHorario || '09:00',
    prazoHorarioFim: form.prazoHorarioFim || null,
    status: form.status,
    tipoServico: form.tipoServico || null,
    etiquetas: form.etiquetasText.split(',').map(item => item.trim()).filter(Boolean),
    checklistItems: form.checklistItems.map((item, index) => ({
      id: item.id,
      texto: item.texto,
      concluido: Boolean(item.concluido),
      ordem: index,
    })),
  }
}

export const STATUS_OPTIONS = [
  { value: 'A_FAZER', label: 'A fazer' },
  { value: 'EM_ANDAMENTO', label: 'Em andamento' },
  { value: 'CONCLUIDA', label: 'Concluída' },
]

export const SERVICE_OPTIONS = [
  { value: 'PINTURA_AO_VIVO', label: 'Pintura ao Vivo' },
  { value: 'AQUARELA_AO_VIVO', label: 'Aquarela ao Vivo' },
  { value: 'ENCOMENDA_ARTISTICA', label: 'Encomenda artística' },
  { value: 'IDENTIDADE_VISUAL', label: 'Identidade visual' },
]

function addDays(dateText, days) {
  if (!dateText) return undefined
  const [year, month, day] = dateText.split('-').map(Number)
  const date = new Date(year, month - 1, day)
  date.setDate(date.getDate() + days)
  return toDateInput(date)
}

function daysBetween(startText, endText) {
  if (!startText || !endText) return 0
  const [startYear, startMonth, startDay] = startText.split('-').map(Number)
  const [endYear, endMonth, endDay] = endText.split('-').map(Number)
  const start = new Date(startYear, startMonth - 1, startDay)
  const end = new Date(endYear, endMonth - 1, endDay)
  return Math.max(Math.round((end.getTime() - start.getTime()) / 86400000), 0)
}

function taskMatchesFilters(task, filters) {
  if (filters.status && task.status !== filters.status) return false
  if (filters.tipoServico && task.tipoServico !== filters.tipoServico) return false
  if (filters.eventId && String(task.event?.id || '') !== String(filters.eventId)) return false
  if (filters.label) {
    const term = filters.label.trim().toLowerCase()
    const labels = task.etiquetas || []
    if (term && !labels.some(label => String(label).toLowerCase().includes(term))) return false
  }
  return true
}

export function useTasksPage(defaultEventId = '') {
  const [tasks, setTasks] = useState([])
  const [events, setEvents] = useState([])
  const [filters, setFilters] = useState({ status: '', tipoServico: '', eventId: defaultEventId, label: '' })
  const [modalOpen, setModalOpen] = useState(false)
  const [selectedTask, setSelectedTask] = useState(null)
  const [form, setForm] = useState({ ...EMPTY_FORM, eventId: defaultEventId })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [commentText, setCommentText] = useState('')
  const { showToast, toast } = useToast()

  // Evento padrão novo (ex.: ao trocar de evento) atualiza o filtro, ajustando o estado na renderização
  const [previousDefaultEventId, setPreviousDefaultEventId] = useState(defaultEventId)
  if (defaultEventId !== previousDefaultEventId) {
    setPreviousDefaultEventId(defaultEventId)
    setFilters(current => {
      const nextEventId = defaultEventId || ''
      return current.eventId === nextEventId ? current : { ...current, eventId: nextEventId }
    })
  }

  // Filtros novos mostram o carregamento já na renderização em que mudam
  const [loadedFilters, setLoadedFilters] = useState(filters)
  if (filters !== loadedFilters) {
    setLoadedFilters(filters)
    setLoading(true)
  }

  // Busca sem setState síncrono: o efeito pode chamá-la direto
  const requestTasks = useCallback(() =>
    tasksService.list(filters)
      .then(data => setTasks(Array.isArray(data) ? data : []))
      .catch(err => showToast(err.message || 'Erro ao carregar tarefas.', 'error'))
      .finally(() => setLoading(false)),
  [filters, showToast])

  const loadTasks = useCallback(async () => {
    setLoading(true)
    await requestTasks()
  }, [requestTasks])

  useEffect(() => {
    requestTasks()
  }, [requestTasks])

  useEffect(() => {
    eventsService.list()
      .then(data => setEvents(Array.isArray(data) ? data : []))
      .catch(() => setEvents([]))
  }, [])

  const calendarEvents = useMemo(() => tasks.map(task => {
    const startTime = task.prazoHorario ? String(task.prazoHorario).slice(0, 5) : '09:00'
    const endTime = task.prazoHorarioFim ? String(task.prazoHorarioFim).slice(0, 5) : '10:00'
    const start = `${task.prazoData}T${startTime}:00`
    const end = task.prazoDataFim
        ? `${task.prazoDataFim}T${endTime}:00`
        : task.prazoHorarioFim
          ? `${task.prazoData}T${String(task.prazoHorarioFim).slice(0, 5)}:00`
          : `${task.prazoData}T${endTime}:00`
    return {
      id: String(task.id),
      title: task.titulo,
      start,
      end,
      classNames: [`task-service-${task.tipoServico || 'GERAL'}`, `task-status-${task.status}`],
      extendedProps: { task },
    }
  }), [tasks])

  const openCreate = (initial = {}) => {
    setSelectedTask(null)
    setForm({ ...EMPTY_FORM, eventId: defaultEventId, ...initial })
    setCommentText('')
    setModalOpen(true)
  }

  const openTask = task => {
    setSelectedTask(task)
    setForm(formFromTask(task))
    setCommentText('')
    setModalOpen(true)
  }

  const setField = (field, value) => {
    setForm(current => ({ ...current, [field]: value }))
  }

  const saveTask = async event => {
    event.preventDefault()
    setSaving(true)
    try {
      const payload = payloadFromForm(form)
      const saved = selectedTask
        ? await tasksService.update(selectedTask.id, payload)
        : await tasksService.create(payload)
      setTasks(current => {
        const withoutSaved = current.filter(task => task.id !== saved.id)
        return taskMatchesFilters(saved, filters) ? [saved, ...withoutSaved] : withoutSaved
      })
      setSelectedTask(saved)
      setForm(formFromTask(saved))
      setModalOpen(false)
      showToast('Tarefa salva com sucesso!', 'success')
      await loadTasks()
    } catch (err) {
      showToast(err.message || 'Erro ao salvar tarefa.', 'error')
    } finally {
      setSaving(false)
    }
  }

  const updateDeadlineFromDrop = async info => {
    const task = info.event.extendedProps.task
    const nextStartDate = toDateInput(info.event.start)
    const originalDurationDays = daysBetween(task.prazoData, task.prazoDataFim || task.prazoData)
    const nextEndDate = info.event.end
      ? toDateInput(info.event.end)
      : (originalDurationDays > 0 ? addDays(nextStartDate, originalDurationDays) : null)

    try {
      await tasksService.updateDeadline(task.id, {
        prazoData: nextStartDate,
        prazoDataFim: nextEndDate,
        prazoHorario: toTimeInput(info.event.start) || '09:00',
        prazoHorarioFim: info.event.end ? toTimeInput(info.event.end) : null,
      })
      showToast('Prazo atualizado.', 'success')
      await loadTasks()
    } catch (err) {
      info.revert()
      showToast(err.message || 'Erro ao atualizar prazo.', 'error')
    }
  }

  const moveTaskToDate = async (task, prazoData) => {
    try {
      await tasksService.updateDeadline(task.id, {
        prazoData,
        prazoHorario: task.prazoHorario ? String(task.prazoHorario).slice(0, 5) : '09:00',
        prazoHorarioFim: task.prazoHorarioFim ? String(task.prazoHorarioFim).slice(0, 5) : null,
      })
      showToast('Prazo atualizado.', 'success')
      await loadTasks()
    } catch (err) {
      showToast(err.message || 'Erro ao atualizar prazo.', 'error')
    }
  }

  const changeStatus = async (task, status) => {
    try {
      const updated = await tasksService.updateStatus(task.id, status)
      setSelectedTask(updated)
      setForm(formFromTask(updated))
      await loadTasks()
    } catch (err) {
      showToast(err.message || 'Erro ao alterar status.', 'error')
    }
  }

  const addChecklistItem = () => {
    setForm(current => ({
      ...current,
      checklistItems: [...current.checklistItems, { texto: '', concluido: false }],
    }))
  }

  const updateChecklistItem = (index, patch) => {
    setForm(current => ({
      ...current,
      checklistItems: current.checklistItems.map((item, currentIndex) => (
        currentIndex === index ? { ...item, ...patch } : item
      )),
    }))
  }

  const removeChecklistItem = index => {
    setForm(current => ({
      ...current,
      checklistItems: current.checklistItems.filter((_, currentIndex) => currentIndex !== index),
    }))
  }

  const addComment = async () => {
    if (!selectedTask || !commentText.trim()) return
    try {
      const updated = await tasksService.addComment(selectedTask.id, commentText)
      setSelectedTask(updated)
      setForm(formFromTask(updated))
      setCommentText('')
      await loadTasks()
    } catch (err) {
      showToast(err.message || 'Erro ao comentar.', 'error')
    }
  }

  const deleteComment = async commentId => {
    if (!selectedTask) return
    try {
      const updated = await tasksService.deleteComment(selectedTask.id, commentId)
      setSelectedTask(updated)
      setForm(formFromTask(updated))
      await loadTasks()
    } catch (err) {
      showToast(err.message || 'Erro ao remover comentário.', 'error')
    }
  }

  const deleteTask = async task => {
    if (!task?.id) return
    try {
      await tasksService.delete(task.id)
      if (selectedTask?.id === task.id) {
        setSelectedTask(null)
        setModalOpen(false)
      }
      showToast('Tarefa excluída com sucesso!', 'success')
      await loadTasks()
    } catch (err) {
      showToast(err.message || 'Erro ao excluir tarefa.', 'error')
    }
  }

  const uploadAttachment = async file => {
    if (!selectedTask || !file) return
    const formData = new FormData()
    formData.append('file', file)
    try {
      const updated = await tasksService.uploadAttachment(selectedTask.id, formData)
      setSelectedTask(updated)
      await loadTasks()
    } catch (err) {
      showToast(err.message || 'Erro ao anexar arquivo.', 'error')
    }
  }

  const deleteAttachment = async attachmentId => {
    if (!selectedTask) return
    try {
      const updated = await tasksService.deleteAttachment(selectedTask.id, attachmentId)
      setSelectedTask(updated)
      await loadTasks()
    } catch (err) {
      showToast(err.message || 'Erro ao remover anexo.', 'error')
    }
  }

  return {
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
    loading,
    modalOpen,
    moveTaskToDate,
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
  }
}
