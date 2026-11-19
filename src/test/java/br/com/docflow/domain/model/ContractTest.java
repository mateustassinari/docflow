package br.com.docflow.domain.model;

import br.com.docflow.domain.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContractTest {

    @Test
    void shouldRequirePdfBeforeSending() {
        Contract contract = new Contract();
        contract.markAsDraft();

        assertThatThrownBy(() -> contract.markAsSent("autentique-id"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PDF nao foi gerado");
    }

    @Test
    void shouldRequireAutentiqueIdWhenSending() {
        Contract contract = new Contract();
        contract.markAsDraft();
        contract.attachPdf("contract.pdf");

        assertThatThrownBy(() -> contract.markAsSent(" "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Identificador do Autentique");
    }

    @Test
    void shouldNotSendSignedContractAgain() {
        Contract contract = new Contract();
        contract.attachPdf("contract.pdf");
        contract.markAsManuallySigned();

        assertThatThrownBy(() -> contract.markAsSent("autentique-id"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Contrato assinado nao pode ser reenviado");
    }

    @Test
    void shouldMarkAsSentWithAutentiqueId() {
        Contract contract = new Contract();
        contract.markAsDraft();
        contract.attachPdf("contract.pdf");

        contract.markAsSent("autentique-id");

        assertThat(contract.getStatus()).isEqualTo(ContractStatus.ENVIADO);
        assertThat(contract.getAutentiqueId()).isEqualTo("autentique-id");
    }

    @Test
    void shouldNotReturnSignedContractToDraft() {
        Contract contract = new Contract();
        contract.markAsManuallySigned();

        assertThatThrownBy(contract::markAsDraftForReview)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Contrato assinado nao pode ser revisado");
    }
}
