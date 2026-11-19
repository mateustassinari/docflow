import { apiFetch } from '../../../shared/api/httpClient'

export const clientsService = {
  list() {
    return apiFetch('/clients')
  },

  create(payload) {
    return apiFetch('/clients', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  update(id, payload) {
    return apiFetch(`/clients/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },

  remove(id) {
    return apiFetch(`/clients/${id}`, {
      method: 'DELETE',
    })
  },
}
