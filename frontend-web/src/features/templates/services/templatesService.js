import { apiFetch } from '../../../shared/api/httpClient'

export const templatesService = {
  list() {
    return apiFetch('/templates')
  },

  create(payload) {
    return apiFetch('/templates', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  update(id, payload) {
    return apiFetch(`/templates/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },

  preview(payload, options = {}) {
    return apiFetch('/templates/preview', {
      ...options,
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
}
