package br.com.docflow.presentation.dto;

import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TemplateContrato;

import java.util.List;

public final class ContractResponseMapper {

    private ContractResponseMapper() {
    }

    public static ContractResponse toResponse(Contract contract) {
        if (contract == null) {
            return null;
        }

        ContractResponse response = new ContractResponse();
        response.setId(contract.getId());
        response.setStatus(contract.getStatus() != null ? contract.getStatus().name() : null);
        response.setHasPdf(contract.getPdfUrl() != null && !contract.getPdfUrl().isBlank());
        response.setAutentiqueId(contract.getAutentiqueId());
        response.setEvent(toEventSummary(contract.getEvent()));
        response.setTemplate(toTemplateSummary(contract.getTemplate()));
        return response;
    }

    public static List<ContractResponse> toResponseList(List<Contract> contracts) {
        return contracts.stream()
                .map(ContractResponseMapper::toResponse)
                .toList();
    }

    private static ContractResponse.EventSummary toEventSummary(Event event) {
        if (event == null) {
            return null;
        }

        ContractResponse.EventSummary summary = new ContractResponse.EventSummary();
        summary.setId(event.getId());
        summary.setTipoServico(event.getTipoServico() != null ? event.getTipoServico().name() : null);
        summary.setDadosServico(event.getDadosServico());
        summary.setTipoEvento(event.getTipoEvento());
        summary.setData(event.getData());
        summary.setHorarioInicio(event.getHorarioInicio());
        summary.setDuracaoEstimada(event.getDuracaoEstimada());
        summary.setLocal(event.getLocal());
        summary.setNumeroConvidados(event.getNumeroConvidados());
        summary.setTecnica(event.getTecnica());
        summary.setTamanhoTela(event.getTamanhoTela());
        summary.setCenaRetratada(event.getCenaRetratada());
        summary.setEntregaObra(event.getEntregaObra());
        summary.setValor(event.getValor());
        summary.setValorSinal(event.getValorSinal());
        summary.setFormaPagamentoSinal(event.getFormaPagamentoSinal());
        summary.setFormaPagamentoRestante(event.getFormaPagamentoRestante());
        summary.setObservacoes(event.getObservacoes());
        summary.setClient(toClientSummary(event.getClient()));
        return summary;
    }

    private static ContractResponse.ClientSummary toClientSummary(Client client) {
        if (client == null) {
            return null;
        }

        ContractResponse.ClientSummary summary = new ContractResponse.ClientSummary();
        summary.setId(client.getId());
        summary.setNome(client.getNome());
        return summary;
    }

    private static ContractResponse.TemplateSummary toTemplateSummary(TemplateContrato template) {
        if (template == null) {
            return null;
        }

        ContractResponse.TemplateSummary summary = new ContractResponse.TemplateSummary();
        summary.setId(template.getId());
        summary.setNome(template.getNome());
        summary.setTipoServico(template.getTipoServico() != null ? template.getTipoServico().name() : null);
        summary.setTipoEvento(template.getTipoEvento());
        summary.setVersao(template.getVersao());
        return summary;
    }
}
