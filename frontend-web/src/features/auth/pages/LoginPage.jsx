import { useState } from 'react'
import { LogIn } from 'lucide-react'
import { APP_NAME } from '../../../shared/config/app'
import { useAuth } from '../AuthContext'

export default function LoginPage() {
  const { login } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async event => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(username, password)
    } catch (err) {
      setError(err.message || 'Não foi possível entrar.')
      setSubmitting(false)
    }
  }

  return (
    <div className="login-page">
      <form className="card login-card" onSubmit={handleSubmit}>
        <div className="login-header">
          <h1>{APP_NAME}</h1>
          <p>Entre para acessar eventos, contratos e tarefas.</p>
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="login-username">Usuário</label>
          <input
            id="login-username"
            className="form-input"
            autoComplete="username"
            value={username}
            onChange={e => setUsername(e.target.value)}
            required
            autoFocus
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="login-password">Senha</label>
          <input
            id="login-password"
            className="form-input"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
        </div>

        {error && <div className="login-error" role="alert">{error}</div>}

        <button className="btn btn-primary login-submit" type="submit" disabled={submitting}>
          <LogIn size={16} />
          {submitting ? 'Entrando...' : 'Entrar'}
        </button>
      </form>
    </div>
  )
}
