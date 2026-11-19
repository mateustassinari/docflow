package br.com.docflow.domain.service;

import br.com.docflow.domain.model.Contract;

public interface ContractDocumentGenerator {
    byte[] generatePdf(Contract contract) throws Exception;
}
