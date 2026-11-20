// Mesma origem da API: em desenvolvimento o Vite faz o proxy de /api, no Docker é o nginx.
export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api'

/** Disparado quando a API responde 401: a sessão expirou ou o servidor reiniciou. */
export const UNAUTHORIZED_EVENT = 'docflow:unauthorized'

const SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS']

function buildUrl(path) {
  if (/^https?:\/\//i.test(path)) return path
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
}

/** Token CSRF que o Spring Security grava no cookie XSRF-TOKEN; volta no header das escritas. */
function csrfToken() {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/)
  return match ? decodeURIComponent(match[1]) : null
}

async function parseErrorResponse(response) {
  const contentType = response.headers.get('content-type') || ''

  if (contentType.includes('application/json')) {
    const data = await response.json().catch(() => ({}))
    return data?.message || 'Erro inesperado'
  }

  const text = await response.text().catch(() => '')
  return text || 'Erro inesperado'
}

export async function apiFetch(path, options = {}) {
  const body = options.body
  // FormData (upload) e URLSearchParams (login) definem o próprio Content-Type
  const setsOwnContentType = body instanceof FormData || body instanceof URLSearchParams
  const method = (options.method || 'GET').toUpperCase()
  const token = SAFE_METHODS.includes(method) ? null : csrfToken()

  const response = await fetch(buildUrl(path), {
    ...options,
    credentials: 'same-origin',
    headers: {
      ...(setsOwnContentType ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { 'X-XSRF-TOKEN': token } : {}),
      ...options.headers,
    },
  })

  if (!response.ok) {
    // As rotas de /auth tratam o 401 na própria tela de login
    if (response.status === 401 && !path.startsWith('/auth/')) {
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
    }
    const error = new Error(await parseErrorResponse(response))
    error.status = response.status
    throw error
  }

  if (response.status === 204) return null

  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    return response.json()
  }

  return response
}

export function apiUrl(path) {
  return buildUrl(path)
}
