import { useCallback, useEffect, useMemo, useState } from 'react'
import { UNAUTHORIZED_EVENT } from '../../shared/api/httpClient'
import { AuthContext } from './AuthContext'
import { authService } from './services/authService'

const ANONYMOUS = { status: 'anonymous', username: null }

function toState(session) {
  return session?.authenticated ? { status: 'authenticated', username: session.username } : ANONYMOUS
}

export default function AuthProvider({ children }) {
  const [state, setState] = useState({ status: 'loading', username: null })

  // Ao abrir: já existe sessão? (um cookie de sessão válido sobrevive a recarregar a página)
  useEffect(() => {
    let active = true
    authService
      .session()
      .then(session => active && setState(toState(session)))
      .catch(() => active && setState(ANONYMOUS))
    return () => {
      active = false
    }
  }, [])

  // Qualquer 401 da API (sessão expirada, servidor reiniciado) volta para o login
  useEffect(() => {
    const onUnauthorized = () => setState(ANONYMOUS)
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
  }, [])

  const login = useCallback(async (username, password) => {
    const session = await authService.login(username, password)
    setState(toState(session))
  }, [])

  const logout = useCallback(async () => {
    try {
      await authService.logout()
    } finally {
      setState(ANONYMOUS)
      // Pede um token CSRF novo para o próximo login (o logout apaga o anterior)
      authService.session().catch(() => {})
    }
  }, [])

  const value = useMemo(() => ({ ...state, login, logout }), [state, login, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
