package br.com.docflow.application.usecase;

import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.domain.repository.ClientRepository;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.application.usecase.command.EventCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateEventUseCase {

    private final ClientRepository clientRepository;
    private final EventRepository eventRepository;

    public Event execute(EventCommand command) {
        Client client = clientRepository.findById(command.clienteId())
                .orElseThrow(() -> new NotFoundException("CLIENT_NOT_FOUND", "Cliente não encontrado"));

        Event event = new Event();
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
