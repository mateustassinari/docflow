export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function buildUrl(path) {
  if (/^https?:\/\//i.test(path)) return path
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
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
  const token = localStorage.getItem('token')
  const body = options.body
  const isFormData = body instanceof FormData

  const response = await fetch(buildUrl(path), {
    ...options,
    headers: {
      ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (!response.ok) {
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
