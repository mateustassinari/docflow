package br.com.docflow.presentation.controller;

import br.com.docflow.application.service.TaskService;
import br.com.docflow.application.port.TaskCalendarDocumentGenerator;
import br.com.docflow.application.readmodel.TaskView;
import br.com.docflow.application.usecase.command.AttachmentUpload;
import br.com.docflow.domain.model.TaskAttachment;
import br.com.docflow.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("null")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskCalendarDocumentGenerator taskCalendarDocumentGenerator;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipoServico,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String label
    ) {
        return ResponseEntity.ok(toResponses(taskService.list(start, end, status, tipoServico, eventId, label)));
    }

    @GetMapping("/summary")
    public ResponseEntity<TaskSummaryResponse> summary() {
        return ResponseEntity.ok(TaskSummaryResponse.from(taskService.summary()));
    }

    @GetMapping(value = "/calendar-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> calendarPdf(
            @RequestParam(required = false, defaultValue = "month") String view,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipoServico,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String label
    ) {
        LocalDate baseDate = date == null ? LocalDate.now() : date;
        LocalDate rangeStart = start != null ? start : resolveStart(view, baseDate);
        LocalDate rangeEnd = end != null ? end : resolveEnd(view, baseDate);
        LocalDate pdfDate = isWeekOrDay(view) ? rangeStart : baseDate;
        byte[] pdf = taskCalendarDocumentGenerator.generate(
                taskService.list(rangeStart, rangeEnd, status, tipoServico, eventId, label),
                view,
                pdfDate
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=calendario-tarefas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<List<TaskResponse>> listByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(toResponses(taskService.listByEvent(eventId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(TaskResponse.from(taskService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(TaskResponse.from(taskService.create(request.toCommand())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(TaskResponse.from(taskService.update(id, request.toCommand())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deadline")
    public ResponseEntity<TaskResponse> updateDeadline(@PathVariable Long id, @Valid @RequestBody TaskDeadlineRequest request) {
        return ResponseEntity.ok(TaskResponse.from(taskService.updateDeadline(id, request.toCommand())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusRequest request) {
        return ResponseEntity.ok(TaskResponse.from(taskService.updateStatus(id, request.getStatus())));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<TaskResponse> addComment(@PathVariable Long id, @Valid @RequestBody TaskCommentRequest request) {
        return ResponseEntity.ok(TaskResponse.from(taskService.addComment(id, request.getTexto())));
    }

    @PutMapping("/{id}/comments/{commentId}")
    public ResponseEntity<TaskResponse> updateComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @Valid @RequestBody TaskCommentRequest request
    ) {
        return ResponseEntity.ok(TaskResponse.from(taskService.updateComment(id, commentId, request.getTexto())));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<TaskResponse> deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
        return ResponseEntity.ok(TaskResponse.from(taskService.deleteComment(id, commentId)));
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TaskResponse> uploadAttachment(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        AttachmentUpload upload = new AttachmentUpload(file.getOriginalFilename(), file.getContentType(), file.getBytes());
        return ResponseEntity.ok(TaskResponse.from(taskService.uploadAttachment(id, upload)));
    }

    @GetMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id, @PathVariable Long attachmentId) throws IOException {
        TaskAttachment attachment = taskService.findAttachment(id, attachmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.originalName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : attachment.contentType()))
                .body(taskService.loadAttachment(attachment));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<TaskResponse> deleteAttachment(@PathVariable Long id, @PathVariable Long attachmentId) throws IOException {
        return ResponseEntity.ok(TaskResponse.from(taskService.deleteAttachment(id, attachmentId)));
    }

    private List<TaskResponse> toResponses(List<TaskView> views) {
        return views.stream().map(TaskResponse::from).toList();
    }

    private LocalDate resolveStart(String view, LocalDate date) {
        if (view != null && view.contains("Week")) {
            return date.minusDays(date.getDayOfWeek().getValue() % 7L);
        }
        if (view != null && view.contains("Day")) {
            return date;
        }
        return date.withDayOfMonth(1);
    }

    private LocalDate resolveEnd(String view, LocalDate date) {
        if (view != null && view.contains("Week")) {
            return resolveStart(view, date).plusDays(6);
        }
        if (view != null && view.contains("Day")) {
            return date;
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    private boolean isWeekOrDay(String view) {
        return view != null && (view.contains("Week") || view.contains("Day") || "week".equals(view) || "day".equals(view));
    }
}
