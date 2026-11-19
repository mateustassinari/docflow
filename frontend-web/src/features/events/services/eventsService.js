import { apiFetch } from '../../../shared/api/httpClient'

export const eventsService = {
  list() {
    return apiFetch('/events')
  },

  get(id) {
    return apiFetch(`/events/${id}`)
  },

  create(payload) {
    return apiFetch('/events', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  update(id, payload) {
    return apiFetch(`/events/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },

  updatePayment(id, payload) {
    return apiFetch(`/events/${id}/payment`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    })
  },
}
