package br.com.docflow.presentation.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class TemplateContratoPreviewRequest extends TemplateContratoRequest {
    private String clienteNome;
    private String clienteCpfCnpj;
    private String clienteEmail;
    private String clienteTelefone;
    private String eventoTipoServico;
    private String eventoDadosServico;
    private String eventoTipo;
    private LocalDate eventoData;
    private String eventoHorarioInicio;
    private String eventoDuracaoEstimada;
    private String eventoLocal;
    private Integer eventoNumeroConvidados;
    private String eventoTecnica;
    private String eventoTamanhoTela;
    private String eventoCenaRetratada;
    private String eventoEntregaObra;
    private BigDecimal eventoValor;
    private BigDecimal eventoValorSinal;
    private String eventoFormaPagamentoSinal;
    private String eventoFormaPagamentoRestante;
    private String eventoObservacoes;
}
