import { Link } from 'react-router-dom'
import TopBar from '../layouts/TopBar'

export default function NotFoundPage() {
  return (
    <>
      <TopBar title="Página não encontrada" />
      <div className="page-content">
        <div className="not-found-page">
          <div className="not-found-card">
            <span className="not-found-kicker">Erro 404</span>
            <h1>Essa página não existe</h1>
            <p>
              O endereço pode estar incorreto, a página pode ter sido movida ou o recurso não está mais disponível.
            </p>
            <div className="not-found-actions">
              <Link className="btn btn-primary" to="/">
                Voltar ao Dashboard
              </Link>
              <Link className="btn btn-secondary" to="/eventos">
                Ver eventos
              </Link>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
