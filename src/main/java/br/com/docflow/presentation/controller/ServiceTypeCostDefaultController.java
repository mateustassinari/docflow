package br.com.docflow.presentation.controller;

import br.com.docflow.presentation.dto.ServiceTypeCostDefaultResponse;
import br.com.docflow.application.usecase.GetServiceTypeCostDefaultsUseCase;
import br.com.docflow.application.usecase.UpdateServiceTypeCostDefaultUseCase;
import br.com.docflow.presentation.dto.ServiceTypeCostDefaultRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cost-defaults")
@RequiredArgsConstructor
public class ServiceTypeCostDefaultController {

    private final GetServiceTypeCostDefaultsUseCase getServiceTypeCostDefaultsUseCase;
    private final UpdateServiceTypeCostDefaultUseCase updateServiceTypeCostDefaultUseCase;

    @GetMapping
    public ResponseEntity<List<ServiceTypeCostDefaultResponse>> getAll() {
        return ResponseEntity.ok(getServiceTypeCostDefaultsUseCase.execute().stream().map(ServiceTypeCostDefaultResponse::from).toList());
    }

    @PutMapping("/{tipoServico}")
    public ResponseEntity<ServiceTypeCostDefaultResponse> update(
            @PathVariable String tipoServico,
            @Valid @RequestBody ServiceTypeCostDefaultRequest request) {
        return ResponseEntity.ok(ServiceTypeCostDefaultResponse.from(updateServiceTypeCostDefaultUseCase.execute(tipoServico, request.getCustoDefault())));
    }
}
