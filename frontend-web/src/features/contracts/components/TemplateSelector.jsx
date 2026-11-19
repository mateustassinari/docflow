import { AlertCircle, Check, FileText } from 'lucide-react'
import { serviceLabel } from '../utils/generateContractUtils'

export default function TemplateSelector({
  disabled,
  selectedTemplateId,
  templates,
  visibleTemplates,
  onSelect,
}) {
  return (
    <div className="card">
      <h2 className="contract-selector-title">Template</h2>
      {templates.length > 0 ? (
        <div className="template-option-list">
          {visibleTemplates.map(template => {
            const selected = String(template.id) === String(selectedTemplateId)
            return (
              <button
                key={template.id}
                type="button"
                disabled={disabled}
                onClick={() => onSelect(String(template.id))}
                className={`template-option ${selected ? 'selected' : ''}`}
              >
                <div className="template-option-icon">
                  <FileText size={16} color={selected ? 'white' : 'var(--primary)'} />
                </div>
                <div className="template-option-text">
                  <div className="template-option-name">{template.nome}</div>
                  <div className="template-option-meta">
                    {serviceLabel(template)} - v{template.versao || '1'}
                  </div>
                </div>
                {selected && <Check size={15} color="var(--primary)" className="template-option-check" />}
              </button>
            )
          })}
        </div>
      ) : (
        <div className="empty-state template-option-empty">
          <AlertCircle size={28} />
          <p>Nenhum template encontrado em /templates.</p>
        </div>
      )}
    </div>
  )
}
