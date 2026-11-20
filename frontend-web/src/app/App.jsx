import { BrowserRouter } from 'react-router-dom'
import { useAuth } from '../features/auth/AuthContext'
import LoginPage from '../features/auth/pages/LoginPage'
import AppProviders from './providers'
import { AppRouter } from './router'

/** Sem sessão, mostra o login no lugar da página pedida; depois do login, a mesma URL abre normalmente. */
function AuthGate() {
  const { status } = useAuth()

  if (status === 'loading') return <div className="login-page" aria-busy="true" />
  if (status === 'anonymous') return <LoginPage />
  return <AppRouter />
}

export default function App() {
  return (
    <AppProviders>
      <BrowserRouter>
        <AuthGate />
      </BrowserRouter>
    </AppProviders>
  )
}
