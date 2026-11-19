import { apiFetch, apiUrl } from '../../../shared/api/httpClient'

export const contractsService = {
  list() {
    return apiFetch('/contracts')
  },

  getByEvent(eventId) {
    return apiFetch(`/contracts/by-event/${eventId}`)
  },

  async getByEventOrNull(eventId) {
    try {
      return await apiFetch(`/contracts/by-event/${eventId}`)
    } catch (error) {
      if (error.status === 404) return null
      throw error
    }
  },

  generate(payload) {
    return apiFetch('/contracts', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  uploadManual(formData) {
    return apiFetch('/contracts/upload', {
      method: 'POST',
      body: formData,
    })
  },

  send(contractId, additionalSigners = []) {
    return apiFetch(`/contracts/${contractId}/send`, {
      method: 'POST',
      body: JSON.stringify({ additionalSigners }),
    })
  },

  requestReview(contractId) {
    return apiFetch(`/contracts/${contractId}/review`, {
      method: 'POST',
    })
  },

  pdfUrl(contractId) {
    return apiUrl(`/contracts/${contractId}/pdf`)
  },
}
