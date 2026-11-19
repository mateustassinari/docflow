package br.com.docflow.presentation.controller;

import br.com.docflow.application.service.AiAssistantFacade;
import br.com.docflow.presentation.dto.AiChatRequest;
import br.com.docflow.presentation.dto.AiChatResponse;
import br.com.docflow.presentation.dto.AiInsightsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAssistantFacade aiAssistantFacade;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(new AiChatResponse(aiAssistantFacade.chat(request.getQuestion())));
    }

    @GetMapping("/insights")
    public ResponseEntity<AiInsightsResponse> insights() {
        return ResponseEntity.ok(new AiInsightsResponse(aiAssistantFacade.insights()));
    }
}
