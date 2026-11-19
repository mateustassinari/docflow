import { ChevronDown } from 'lucide-react'
import { formatDate } from '../utils/generateContractUtils'

export default function EventSelector({
  availableEventos,
  disabled,
  selectedEventId,
  onSelect,
}) {
  return (
    <div className="card">
      <h2 className="contract-selector-title">Evento</h2>
      <div className="form-group">
        <label className="form-label">Selecione o evento</label>
        <div className="select-with-icon">
          <select
            className="form-select select-with-icon-input"
            value={selectedEventId}
            onChange={e => onSelect(e.target.value)}
            disabled={disabled}
          >
            <option value="">Selecione</option>
            {availableEventos.map(evento => (
              <option key={evento.id} value={evento.id}>
                {evento.client?.nome || 'Cliente'} - {evento.tipoEvento || 'Evento'} - {formatDate(evento.data)}
              </option>
            ))}
          </select>
          <ChevronDown size={15} className="select-with-icon-chevron" />
        </div>
      </div>
    </div>
  )
}
