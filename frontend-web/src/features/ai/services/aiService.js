import { apiFetch } from '../../../shared/api/httpClient'

export const aiService = {
  chat(question) {
    return apiFetch('/ai/chat', {
      method: 'POST',
      body: JSON.stringify({ question }),
    })
  },

  getStatus() {
    return apiFetch('/ai/status')
  },

  getInsights() {
    return apiFetch('/ai/insights')
  },
}
