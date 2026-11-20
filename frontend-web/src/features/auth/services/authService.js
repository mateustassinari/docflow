import { apiFetch } from '../../../shared/api/httpClient'

export const authService = {
  /** { authenticated, username }; também entrega o cookie do token CSRF. */
  session: () => apiFetch('/auth/session'),
  login: (username, password) =>
    apiFetch('/auth/login', { method: 'POST', body: new URLSearchParams({ username, password }) }),
  logout: () => apiFetch('/auth/logout', { method: 'POST' }),
}
