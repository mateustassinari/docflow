import { NavLink, useNavigate } from 'react-router-dom'
import { APP_NAME } from '../shared/config/app'
import {
  LayoutDashboard,
  Calendar,
  ListTodo,
  Users,
  LayoutTemplate,
  TrendingUp,
  Sparkles,
  Plus
} from 'lucide-react'

const navItems = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/eventos', icon: Calendar, label: 'Eventos' },
  { to: '/financeiro', icon: TrendingUp, label: 'Financeiro' },
  { to: '/tarefas', icon: ListTodo, label: 'Tarefas' },
  { to: '/ia', icon: Sparkles, label: 'Assistente IA' },
  { to: '/clientes', icon: Users, label: 'Clientes' },
  { to: '/templates', icon: LayoutTemplate, label: 'Templates' },
]

export default function Sidebar() {
  const navigate = useNavigate()

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="sidebar-logo-box">
          <div className="sidebar-logo-text">
            <h2>{APP_NAME}</h2>
            <span>Eventos e Contratos</span>
          </div>
        </div>
      </div>

      <nav className="sidebar-nav">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <Icon size={16} />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-bottom">
        <button className="btn-new-contract" onClick={() => navigate('/eventos/novo')}>
          <Plus size={16} />
          Novo Evento
        </button>
      </div>
    </aside>
  )
}
