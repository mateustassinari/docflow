package br.com.docflow.presentation.controller;

import br.com.docflow.application.usecase.CreateEventUseCase;
import br.com.docflow.application.usecase.UpdateEventPaymentUseCase;
import br.com.docflow.application.usecase.UpdateEventUseCase;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.presentation.dto.EventPaymentRequest;
import br.com.docflow.presentation.dto.EventRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final CreateEventUseCase createEventUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final UpdateEventPaymentUseCase updateEventPaymentUseCase;

    @PostMapping
    public ResponseEntity<Event> create(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(createEventUseCase.execute(request.toCommand()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(updateEventUseCase.execute(id, request.toCommand()));
    }

    @GetMapping
    public ResponseEntity<List<Event>> listAll() {
        return ResponseEntity.ok(eventRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> findById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/payment")
    public ResponseEntity<Event> updatePayment(@PathVariable Long id, @RequestBody EventPaymentRequest request) {
        return ResponseEntity.ok(updateEventPaymentUseCase.execute(id, request.toCommand()));
    }
}
