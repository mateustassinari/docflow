package br.com.docflow.application.usecase;

import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.domain.repository.ClientRepository;
import br.com.docflow.domain.repository.ContractRepository;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.application.usecase.command.EventCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UpdateEventUseCase {

    private final ClientRepository clientRepository;
    private final EventRepository eventRepository;
    private final ContractRepository contractRepository;

    @Transactional
    public Event execute(Long eventId, EventCommand command) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("EVENT_NOT_FOUND", "Evento nao encontrado"));

        Client client = clientRepository.findById(command.clienteId())
                .orElseThrow(() -> new NotFoundException("CLIENT_NOT_FOUND", "Cliente nao encontrado"));

        Contract contract = contractRepository.findByEventId(eventId).orElse(null);
        boolean contractualChanged = changedContractualData(event, command);

        if (contract != null && contractualChanged) {
            if (contract.getStatus() == ContractStatus.RASCUNHO) {
                contractRepository.deleteById(contract.getId());
            } else if (contract.getStatus() == ContractStatus.ENVIADO || contract.getStatus() == ContractStatus.ASSINADO) {
                throw new BusinessException(
                        "EVENT_CONTRACTUAL_DATA_LOCKED",
                        "Dados contratuais bloqueados para este status do contrato"
                );
            }
        }

        event.setClient(client);
        event.setTipoServico(parseTipoServico(command.tipoServico()));
        event.setDadosServico(command.dadosServico());
        event.setTipoEvento(command.tipoEvento());
        event.setData(command.data());
        event.setHorarioInicio(command.horarioInicio());
        event.setDuracaoEstimada(command.duracaoEstimada());
        event.setLocal(command.local());
        event.setNumeroConvidados(command.numeroConvidados());
        event.setTecnica(command.tecnica());
        event.setTamanhoTela(command.tamanhoTela());
        event.setCenaRetratada(command.cenaRetratada());
        event.setEntregaObra(command.entregaObra());
        event.setValor(command.valor());
        event.setValorSinal(command.valorSinal());
        event.setFormaPagamentoSinal(command.formaPagamentoSinal());
        event.setFormaPagamentoRestante(command.formaPagamentoRestante());
        event.setObservacoes(command.observacoes());
        event.setContratoDispensado(command.contratoDispensado());

        return eventRepository.save(event);
    }

    private boolean changedContractualData(Event event, EventCommand command) {
        Long currentClientId = event.getClient() != null ? event.getClient().getId() : null;

        return !Objects.equals(currentClientId, command.clienteId())
                || !Objects.equals(event.getTipoServico(), parseTipoServico(command.tipoServico()))
                || !Objects.equals(event.getDadosServico(), command.dadosServico())
                || !Objects.equals(event.getTipoEvento(), command.tipoEvento())
                || !Objects.equals(event.getData(), command.data())
                || !Objects.equals(event.getHorarioInicio(), command.horarioInicio())
                || !Objects.equals(event.getDuracaoEstimada(), command.duracaoEstimada())
                || !Objects.equals(event.getLocal(), command.local())
                || !Objects.equals(event.getNumeroConvidados(), command.numeroConvidados())
                || !Objects.equals(event.getTecnica(), command.tecnica())
                || !Objects.equals(event.getTamanhoTela(), command.tamanhoTela())
                || !Objects.equals(event.getCenaRetratada(), command.cenaRetratada())
                || !Objects.equals(event.getEntregaObra(), command.entregaObra())
                || !sameValue(event.getValor(), command.valor())
                || !sameValue(event.getValorSinal(), command.valorSinal())
                || !Objects.equals(event.getFormaPagamentoSinal(), command.formaPagamentoSinal())
                || !Objects.equals(event.getFormaPagamentoRestante(), command.formaPagamentoRestante());
    }

    private boolean sameValue(java.math.BigDecimal current, java.math.BigDecimal next) {
        if (current == null || next == null) {
            return current == next;
        }

        return current.compareTo(next) == 0;
    }

    private TipoServico parseTipoServico(String tipoServico) {
        String value = tipoServico;
        if (value == null || value.isBlank()) {
            return TipoServico.PINTURA_AO_VIVO;
        }

        return switch (value.trim().toUpperCase().replace(' ', '_')) {
            case "AQUARELA_AO_VIVO" -> TipoServico.AQUARELA_AO_VIVO;
            case "ENCOMENDA_ARTISTICA" -> TipoServico.ENCOMENDA_ARTISTICA;
            case "IDENTIDADE_VISUAL" -> TipoServico.IDENTIDADE_VISUAL;
            default -> TipoServico.PINTURA_AO_VIVO;
        };
    }
}
