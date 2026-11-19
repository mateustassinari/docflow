package br.com.docflow.presentation.dto;

import br.com.docflow.domain.model.AdditionalSigner;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SendContractRequest {
    private List<AdditionalSigner> additionalSigners = new ArrayList<>();
}
