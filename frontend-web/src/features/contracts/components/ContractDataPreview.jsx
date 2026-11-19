import { formatDate, formatMoney, formatTime, isLiveService, serviceLabel } from '../utils/generateContractUtils'

export default function ContractDataPreview({
  selectedEvent,
  selectedTemplate,
}) {
  const selectedEventIsLive = isLiveService(selectedEvent?.tipoServico)

  return (
    <>
      <div className="contract-data-grid">
        <div>
          <div className="form-label">Serviço</div>
          <strong>{serviceLabel(selectedEvent)}</strong>
        </div>
        <div>
          <div className="form-label">Cliente</div>
          <strong>{selectedEvent?.client?.nome || 'Selecione um evento'}</strong>
        </div>
        {selectedEventIsLive && (
          <>
            <div>
              <div className="form-label">Evento</div>
              <strong>{selectedEvent?.tipoEvento || '-'}</strong>
            </div>
            <div>
              <div className="form-label">Data</div>
              <strong>{formatDate(selectedEvent?.data)}</strong>
            </div>
          </>
        )}
        <div>
          <div className="form-label">Valor</div>
          <strong>{formatMoney(selectedEvent?.valor)}</strong>
        </div>
        {selectedEventIsLive && (
          <>
            <div>
              <div className="form-label">Horário de início</div>
              <strong>{formatTime(selectedEvent?.horarioInicio) || '-'}</strong>
            </div>
            <div>
              <div className="form-label">Convidados</div>
              <strong>{selectedEvent?.numeroConvidados || '-'}</strong>
            </div>
            <div>
              <div className="form-label">Técnica</div>
              <strong>{selectedEvent?.tecnica || '-'}</strong>
            </div>
            <div>
              <div className="form-label">Tela</div>
              <strong>{selectedEvent?.tamanhoTela || '-'}</strong>
            </div>
          </>
        )}
        <div>
          <div className="form-label">Pagamento sinal</div>
          <strong>{selectedEvent?.formaPagamentoSinal || '-'}</strong>
        </div>
        <div>
          <div className="form-label">Pagamento restante</div>
          <strong>{selectedEvent?.formaPagamentoRestante || '-'}</strong>
        </div>
      </div>

      <div className="contract-selected-template">
        <div className="form-label">Template selecionado</div>
        <strong>{selectedTemplate?.nome || 'Nenhum template selecionado'}</strong>
        <p>
          {selectedTemplate
            ? `${serviceLabel(selectedTemplate)} - v${selectedTemplate.versao || '1'}`
            : 'Selecione um template compatível com o serviço do evento.'}
        </p>
      </div>
    </>
  )
}
