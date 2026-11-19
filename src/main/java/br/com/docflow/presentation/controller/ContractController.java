package br.com.docflow.presentation.controller;

import br.com.docflow.application.usecase.FindContractByEventUseCase;
import br.com.docflow.application.usecase.FindContractByIdUseCase;
import br.com.docflow.application.usecase.GenerateContractUseCase;
import br.com.docflow.application.usecase.GetContractPdfUseCase;
import br.com.docflow.application.usecase.ListContractsUseCase;
import br.com.docflow.application.usecase.ReviewContractUseCase;
import br.com.docflow.application.usecase.SendContractUseCase;
import br.com.docflow.application.usecase.UploadContractPdfUseCase;
import br.com.docflow.presentation.dto.ContractResponse;
import br.com.docflow.presentation.dto.ContractResponseMapper;
import br.com.docflow.presentation.dto.GenerateContractRequest;
import br.com.docflow.presentation.dto.SendContractRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("null")
@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final GenerateContractUseCase generateUseCase;
    private final SendContractUseCase sendUseCase;
    private final ListContractsUseCase listContractsUseCase;
    private final FindContractByIdUseCase findContractByIdUseCase;
    private final FindContractByEventUseCase findContractByEventUseCase;
    private final GetContractPdfUseCase getContractPdfUseCase;
    private final ReviewContractUseCase reviewContractUseCase;
    private final UploadContractPdfUseCase uploadContractPdfUseCase;

    @PostMapping
    public ResponseEntity<ContractResponse> generate(@Valid @RequestBody GenerateContractRequest request) {
        return ResponseEntity.ok(
                ContractResponseMapper.toResponse(generateUseCase.execute(request.getEventId(), request.getTemplateId()))
        );
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractResponse> upload(
            @RequestParam Long eventId,
            @RequestParam Long templateId,
            @RequestParam(defaultValue = "false") boolean signed,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(
                ContractResponseMapper.toResponse(uploadContractPdfUseCase.execute(eventId, templateId, file.getBytes(), signed))
        );
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<ContractResponse> sendForSignature(
            @PathVariable Long id,
            @RequestBody(required = false) SendContractRequest request
    ) {
        return ResponseEntity.ok(ContractResponseMapper.toResponse(
                sendUseCase.execute(id, request == null ? List.of() : request.getAdditionalSigners())
        ));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<ContractResponse> review(@PathVariable Long id) {
        return ResponseEntity.ok(ContractResponseMapper.toResponse(reviewContractUseCase.execute(id)));
    }

    @GetMapping
    public ResponseEntity<List<ContractResponse>> listAll() {
        return ResponseEntity.ok(ContractResponseMapper.toResponseList(listContractsUseCase.execute()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ContractResponseMapper.toResponse(findContractByIdUseCase.execute(id)));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> viewPdf(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=contract-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(getContractPdfUseCase.execute(id));
    }

    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<ContractResponse> findByEventId(@PathVariable Long eventId) {
        return ResponseEntity.ok(ContractResponseMapper.toResponse(findContractByEventUseCase.execute(eventId)));
    }
}
