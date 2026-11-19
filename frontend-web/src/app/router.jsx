import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import DashboardLayout from '../layouts/DashboardLayout'
import NotFoundPage from './NotFoundPage'
import AiPage from '../features/ai/pages/AiPage'
import ClientsPage from '../features/clients/pages/ClientsPage'
import GenerateContractPage from '../features/contracts/pages/GenerateContractPage'
import DashboardPage from '../features/dashboard/pages/DashboardPage'
import EventDetailPage from '../features/events/pages/EventDetailPage'
import EventsPage from '../features/events/pages/EventsPage'
import NewEventPage from '../features/events/pages/NewEventPage'
import FinancialPage from '../features/financial/pages/FinancialPage'
import TasksPage from '../features/tasks/pages/TasksPage'
import TemplatePreviewWindow from '../features/templates/pages/TemplatePreviewWindow'
import TemplatesPage from '../features/templates/pages/TemplatesPage'

export function AppRouter() {
  const location = useLocation()

  if (location.pathname === '/templates/preview-window') {
    return (
      <Routes>
        <Route path="/templates/preview-window" element={<TemplatePreviewWindow />} />
      </Routes>
    )
  }

  return (
    <Routes>
      <Route element={<DashboardLayout />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/eventos" element={<EventsPage />} />
        <Route path="/eventos/novo" element={<NewEventPage />} />
        <Route path="/eventos/:id" element={<EventDetailPage />} />
        <Route path="/financeiro" element={<FinancialPage />} />
        <Route path="/tarefas" element={<TasksPage />} />
        <Route path="/ia" element={<AiPage />} />
        <Route path="/clientes" element={<ClientsPage />} />
        <Route path="/contratos" element={<Navigate to="/eventos" replace />} />
        <Route path="/contratos/gerar" element={<GenerateContractPage />} />
        <Route path="/templates" element={<TemplatesPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
