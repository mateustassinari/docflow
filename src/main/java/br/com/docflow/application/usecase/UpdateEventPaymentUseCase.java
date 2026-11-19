package br.com.docflow.application.usecase;

import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.application.usecase.command.UpdateEventPaymentCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateEventPaymentUseCase {

    private final EventRepository eventRepository;

    @Transactional
    public Event execute(Long eventId, UpdateEventPaymentCommand command) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("EVENT_NOT_FOUND", "Evento não encontrado"));

        event.setSinalRecebido(command.sinalRecebido());
        event.setDataRecebimentoSinal(command.dataRecebimentoSinal());
        event.setRestanteRecebido(command.restanteRecebido());
        event.setDataRecebimentoRestante(command.dataRecebimentoRestante());
        event.setObservacaoPagamento(command.observacaoPagamento());
        event.setCustoTotal(command.custoTotal());
        event.setCustosExtras(command.custosExtras());

        return eventRepository.save(event);
    }
}
