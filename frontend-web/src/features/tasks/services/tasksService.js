import { apiFetch, apiUrl } from '../../../shared/api/httpClient'

function buildQuery(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.set(key, value)
  })
  const text = query.toString()
  return text ? `?${text}` : ''
}

export const tasksService = {
  list(params) {
    return apiFetch(`/tasks${buildQuery(params)}`)
  },

  listByEvent(eventId) {
    return apiFetch(`/tasks/by-event/${eventId}`)
  },

  summary() {
    return apiFetch('/tasks/summary')
  },

  get(id) {
    return apiFetch(`/tasks/${id}`)
  },

  create(payload) {
    return apiFetch('/tasks', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  update(id, payload) {
    return apiFetch(`/tasks/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },

  delete(id) {
    return apiFetch(`/tasks/${id}`, {
      method: 'DELETE',
    })
  },

  updateDeadline(id, payload) {
    return apiFetch(`/tasks/${id}/deadline`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    })
  },

  updateStatus(id, status) {
    return apiFetch(`/tasks/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    })
  },

  addComment(id, texto) {
    return apiFetch(`/tasks/${id}/comments`, {
      method: 'POST',
      body: JSON.stringify({ texto }),
    })
  },

  updateComment(id, commentId, texto) {
    return apiFetch(`/tasks/${id}/comments/${commentId}`, {
      method: 'PUT',
      body: JSON.stringify({ texto }),
    })
  },

  deleteComment(id, commentId) {
    return apiFetch(`/tasks/${id}/comments/${commentId}`, {
      method: 'DELETE',
    })
  },

  uploadAttachment(id, formData) {
    return apiFetch(`/tasks/${id}/attachments`, {
      method: 'POST',
      body: formData,
    })
  },

  deleteAttachment(id, attachmentId) {
    return apiFetch(`/tasks/${id}/attachments/${attachmentId}`, {
      method: 'DELETE',
    })
  },

  attachmentUrl(id, attachmentId) {
    return apiUrl(`/tasks/${id}/attachments/${attachmentId}`)
  },

  calendarPdfUrl(params = {}) {
    return apiUrl(`/tasks/calendar-pdf${buildQuery(params)}`)
  },
}
