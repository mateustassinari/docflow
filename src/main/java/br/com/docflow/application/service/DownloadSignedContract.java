package br.com.docflow.application.service;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.service.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DownloadSignedContract {

    private final SignatureService signatureService;

    public byte[] execute(Contract contract) throws Exception {
        return signatureService.downloadSignedPdf(contract.getAutentiqueId());
    }
}
