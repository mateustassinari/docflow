import { apiFetch } from '../../../shared/api/httpClient'

export const financialService = {
  getCostDefaults() {
    return apiFetch('/cost-defaults')
  },

  updateCostDefault(tipoServico, custoDefault) {
    return apiFetch(`/cost-defaults/${tipoServico}`, {
      method: 'PUT',
      body: JSON.stringify({ custoDefault }),
    })
  },
}
