package br.com.docflow.domain.service;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.AdditionalSigner;

import java.util.List;

public interface SignatureService {
    String sendForSignature(Contract contract, byte[] pdfBytes, List<AdditionalSigner> additionalSigners) throws Exception;
    boolean isContractSigned(String autentiqueId) throws Exception;
    byte[] downloadSignedPdf(String autentiqueId) throws Exception;
}
